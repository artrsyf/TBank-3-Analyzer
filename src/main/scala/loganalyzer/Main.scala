import scala.io.Source
import scala.util.Using

import cats.effect.{IO, IOApp, ExitCode}

import java.io.InputStream
import java.nio.file.Files
import java.time.OffsetDateTime

import loganalyzer.shared.configs.FormatterIso8601

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecordFilter.NginxLogRecordFilter

import loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport.GeneralLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResponseCodesLogReport.ResponseCodesLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport.UserAgentsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.HttpMethodsLogReport.HttpMethodsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport.ResourcesLogReport

import loganalyzer.application.FileGenerator.FileGenerator
import loganalyzer.application.FileReader.FileReader
import loganalyzer.application.parser.CommandLineParser.CommandLineParser

import loganalyzer.shared.constants.Report.*

object Main extends IOApp:

  def run(args: List[String]) =
    CommandLineParser.run(args).flatMap {
      case Left(error) =>
        IO(println("Error parsing command line arguments")) *> IO(
          ExitCode.Error
        )
      case Right(config) =>
        val filePath = config.path
        val reportFormat = config.format

        val fromDate = config.from match
          case Some(value) =>
            OffsetDateTime.parse(value, FormatterIso8601)
          case _ =>
            OffsetDateTime.MIN

        val toDate = config.to match
          case Some(value) =>
            OffsetDateTime.parse(value, FormatterIso8601)
          case _ =>
            OffsetDateTime.MAX

        val filter = NginxLogRecordFilter()
        val filterFunction = filter
          .createFilterFunction(config.filterField, config.filterValue)

        val reportList = Array(
          GeneralLogReport(),
          ResponseCodesLogReport(),
          UserAgentsLogReport(),
          HttpMethodsLogReport(),
          ResourcesLogReport(tenPopularRecordBound = true)
        )

        val logLines: IO[Iterator[String]] =
          FileReader
            .readFiles(filePath)
            .flatMap { inputStreams =>
              IO {
                inputStreams.iterator.flatMap { inputStream =>
                  Using(Source.fromInputStream(inputStream)) { source =>
                    source.getLines()
                  }.getOrElse(Iterator.empty)
                }
              }
            }
            .handleErrorWith { e =>
              IO {
                println(s"Error reading file(s): ${e.getMessage}")
                Iterator.empty
              }
            }

        logLines.flatMap { lines =>
          FileReader.readFileNames(filePath).flatMap { fileNames =>
            val updatedReports =
              reportList.map(report =>
                report match
                  case generalReport: GeneralLogReport =>
                    generalReport.copy(fileNames = fileNames)
                  case otherReport => otherReport
              )

            val updatedReportsAfterProcessing =
              lines.foldLeft(updatedReports) { (reports, logLine) =>
                val clearLogLine = logLine.trim()
                val logRecord =
                  NginxLogRecord(clearLogLine)

                if (
                    (
                      logRecord.requestTimeStamp.isAfter(fromDate) ||
                        logRecord.requestTimeStamp.isEqual(fromDate)
                    ) &&
                    (
                      logRecord.requestTimeStamp.isBefore(toDate) ||
                        logRecord.requestTimeStamp.isEqual(toDate)
                    ) &&
                    filterFunction(logRecord)
                  )
                then
                  reports
                    .map(report => report.updateWithSingleIteration(logRecord))
                else reports
              }

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

            FileGenerator
              .createFile("./report_dist", finalReportName, finalReport) *>
              IO(ExitCode.Success)
          }
        }
    }

  private def createFilterFunction(
    filterField: Option[String],
    filterValue: Option[String]
  ): NginxLogRecord => Boolean =
    (filterField, filterValue) match
      case (Some("address"), Some(value)) =>
        record => record.remoteAddress.contains(value)
      case (Some("method"), Some(value)) =>
        record =>
          record.requestMethod.toString.toLowerCase() == value.toLowerCase()
      case (Some("url"), Some(value)) =>
        record => record.requestUrl.contains(value)
      case (Some("protocol"), Some(value)) =>
        record => record.httpVersion.toLowerCase() == value.toLowerCase()
      case (Some("response-code"), Some(value)) =>
        record => record.responseCode == value.toInt
      case (Some("response-size"), Some(value)) =>
        record => record.responseSize == value.toInt
      case (Some("referer"), Some(value)) =>
        record => record.referer.toLowerCase() == value.toLowerCase()
      case (Some("user-agent"), Some(value)) =>
        record => record.userAgent.toLowerCase().contains(value.toLowerCase())
      case _ => _ => true
