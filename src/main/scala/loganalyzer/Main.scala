import cats.effect.{IO, IOApp, ExitCode}

import scala.io.Source
import scala.util.Using
import scala.util.{Try, Success, Failure}

import java.io.FileInputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

import loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport.GeneralLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResponseCodesLogReport.ResponseCodesLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport.UserAgentsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.HttpMethodsLogReport.HttpMethodsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport.ResourcesLogReport

import loganalyzer.application.FileGenerator.FileGenerator

import loganalyzer.shared.constants.Report.*

import loganalyzer.application.parser.CommandLineParser.CommandLineParser

object Main extends IOApp:
  
  def run(args: List[String]) =
    CommandLineParser.run(args).flatMap {
      case Left(error) => IO.raiseError(new Exception("Error parsing command line arguments"))
      case Right(config) =>
        val filePath = config.path
        val reportFormat = config.format
        val fromDate = config.from match
          case Some(value) => 
            val formatter = DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)
            OffsetDateTime.parse(value, formatter)
          case _ => 
            OffsetDateTime.MIN

        val filterFunction: NginxLogRecord => Boolean = createFilterFunction(config.filterField, config.filterValue)

        val reportList = Array(
          GeneralLogReport(fileName = filePath.split("/").last),
          ResponseCodesLogReport(),
          UserAgentsLogReport(),
          HttpMethodsLogReport(),
          ResourcesLogReport(tenPopularRecordBound = true)
        )

        val logLines: IO[List[String]] = IO {
          Using(Source.fromInputStream(new FileInputStream(filePath))) { source =>
            source.getLines().toList
          }.getOrElse {
            List.empty[String]
          }
        }.handleErrorWith { e =>
          IO {
            println(s"Error reading file: ${e.getMessage}")
            List.empty[String]
          }
        }

        logLines.flatMap { lines => 
          val updatedReports = lines.foldLeft(reportList) { (reports, logLine) =>
            val clearLogLine = logLine.trim()
            val logRecord = NginxLogRecord.newLogRecordFromString(clearLogLine)

            if ((logRecord.requestTimeStamp.isAfter(fromDate) ||
                logRecord.requestTimeStamp.isEqual(fromDate)) &&
                filterFunction(logRecord)) then
              reports.map(report => report.updateWithSingleIteration(logRecord))
            else
              reports
          }

          val (finalReport, finalReportName) = reportFormat match
            case MarkdownFormatNaming =>
              (
                updatedReports.foldLeft("") { (acc, current) =>
                  acc + s"\n${current.generateMarkdownReport()}"
                },
                "report.md"
              )
            case AdocFormatNaming =>
              (
                updatedReports.foldLeft("") { (acc, current) =>
                  acc + s"\n${current.generateAsciidocReport()}"
                },
              "report.adoc"
              )
            case _ => 
              (
                "Given unsupported report format.",
                "report_error_log.txt"
              )
          
          FileGenerator.createFile("./report_dist", finalReportName, finalReport) *>
            IO(ExitCode.Success)
        }
    }
      

  private def createFilterFunction(filterField: Option[String], filterValue: Option[String]): NginxLogRecord => Boolean =
    (filterField, filterValue) match
      case (Some("address"), Some(value))    => record => record.remoteAddress.contains(value)
      case (Some("method"), Some(value)) => record => record.requestMethod.toString.toLowerCase() == value.toLowerCase()
      case (Some("url"), Some(value))    => record => record.requestUrl.contains(value)
      case (Some("protocol"), Some(value))    => record => record.httpVersion.toLowerCase() == value.toLowerCase()
      case (Some("response-code"), Some(value))    => record => record.responseCode == value.toInt
      case (Some("response-size"), Some(value))    => record => record.responseSize == value.toInt
      case (Some("referer"), Some(value))    => record => record.referer.toLowerCase() == value.toLowerCase()
      case (Some("user-agent"), Some(value))    => record => record.userAgent.toLowerCase().contains(value.toLowerCase())
      case _                             => _ => true