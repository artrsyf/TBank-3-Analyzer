package loganalyzer.application.NginxLogProcessor

import java.time.OffsetDateTime
import java.io.{BufferedReader, InputStream, InputStreamReader}

import scala.io.Source
import scala.util.Using

import cats.effect.{IO, Resource}

import loganalyzer.application.parser.CommandLineParser.Config
import loganalyzer.shared.configs.FormatterIso8601
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord
import loganalyzer.application.FileReader.FileReader
import loganalyzer.application.FileGenerator.FileGenerator

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport.GeneralLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResponseCodesLogReport.ResponseCodesLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport.UserAgentsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.HttpMethodsLogReport.HttpMethodsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport.ResourcesLogReport

import loganalyzer.shared.constants.Report.*

class NginxLogProcessor(
    config: Config,
    fileReader: FileReader,
    fileGenerator: FileGenerator,
    filterFunction: NginxLogRecord => Boolean
):
    private def filePath = config.path

    private def reportFormat = config.format

    private def fromDate = config.from match
        case Some(value) =>
            OffsetDateTime.parse(value, FormatterIso8601)
        case _ =>
            OffsetDateTime.MIN

    private def toDate = config.to match
        case Some(value) =>
            OffsetDateTime.parse(value, FormatterIso8601)
        case _ =>
            OffsetDateTime.MAX

    private def emptyReportList = Array(
          GeneralLogReport(),
          ResponseCodesLogReport(),
          UserAgentsLogReport(),
          HttpMethodsLogReport(),
          ResourcesLogReport(tenPopularRecordBound = true)
        )
    
    def lazyReadFiles: IO[Iterator[String]] = {
      fileReader.readFiles(filePath).flatMap { inputStreams =>
        val resource = Resource.make(IO(inputStreams.iterator)) { _ =>
          IO.unit
        }
        
        resource.use { iterator =>
          IO {
            iterator.flatMap { inputStream =>
              val reader = new BufferedReader(new InputStreamReader(inputStream))
              Iterator.continually(reader.readLine()).takeWhile(_ != null).map(_.trim)
            }
          }
        }
      }
    }

    def logRecordInvariant(logRecord: NginxLogRecord): Boolean = 
        (
            logRecord.requestTimeStamp.isAfter(fromDate) ||
            logRecord.requestTimeStamp.isEqual(fromDate)
        ) &&
        (
            logRecord.requestTimeStamp.isBefore(toDate) ||
            logRecord.requestTimeStamp.isEqual(toDate)
        ) &&
        filterFunction(logRecord)

    def updateReportsAfterProcessing(initReports: Array[LogReport]): IO[Array[LogReport]] = 
      lazyReadFiles.flatMap { logLinesIterator =>
        fileReader.readFileNames(filePath).flatMap { fileNames =>
          val updatedReports = initReports.map {
            case generalReport: GeneralLogReport =>
              generalReport.copy(fileNames = fileNames)
            case otherReport => otherReport
          }

          logLinesIterator.foldLeft(IO.pure(updatedReports)) { (ioReports, logLine) =>
            ioReports.flatMap { reports =>
              val logRecord = NginxLogRecord(logLine)

              val updatedReports = 
                if (logRecordInvariant(logRecord)) {
                  reports.map(report => report.updateWithSingleIteration(logRecord))
                } else {
                  reports
                }

              IO.pure(updatedReports)
            }
          }
        }
      }
    
    def assemblyReport(updatedReportsAfterProcessing: Array[LogReport]) = 
        val (finalReport, finalReportName) = reportFormat match
              case MarkdownFormatNaming =>
                (
                  updatedReportsAfterProcessing.foldLeft("") { (acc, current) =>
                    acc + s"\n${current.generateMarkdownReport()}"
                  },
                  "report.md"
                )
              case AdocFormatNaming =>
                (
                  updatedReportsAfterProcessing.foldLeft("") { (acc, current) =>
                    acc + s"\n${current.generateAsciidocReport()}"
                  },
                  "report.adoc"
                )
              case _ =>
                (
                  "Given unsupported report format.",
                  "report_error_log.txt"
                )
        
        (finalReport, finalReportName)

    def generateReport = 
        updateReportsAfterProcessing(emptyReportList).flatMap { readyReports =>
            val (report, reportName) = assemblyReport(readyReports)
            
            fileGenerator
              .createFile("./report_dist", reportName, report)
        }
