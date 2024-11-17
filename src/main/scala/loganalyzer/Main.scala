import scala.io.Source
import scala.io.Codec
import java.nio.charset.CodingErrorAction
import scala.util.Using
import scala.util.{Try, Success, Failure}

import java.io.{FileOutputStream, FileInputStream}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.io.{OutputStreamWriter}
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

import loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport.GeneralLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResponseCodesLogReport.ResponseCodesLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport.UserAgentsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.HttpMethodsLogReport.HttpMethodsLogReport
import loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport.ResourcesLogReport

import loganalyzer.application.FileGenerator.FileGenerator

import loganalyzer.shared.constants.Report.*

import loganalyzer.application.parser.CommandLineParser.CommandLineParser
import scala.annotation.switch

object Main:

  def main(args: Array[String]): Unit =
    CommandLineParser.run(args) match
      case Left(error) => println("Issue...")
      case Right(config) =>
        val filePath = config.path
        val reportFormat = config.format
        val fromDate = config.from match
          case Some(value) => 
            val formatter = DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)
            OffsetDateTime.parse(value, formatter)
          case _ => 
            OffsetDateTime.MIN

        val reportList = Array(
          GeneralLogReport(fileName = filePath.split("/").last),
          ResponseCodesLogReport(),
          UserAgentsLogReport(),
          HttpMethodsLogReport(),
          ResourcesLogReport(tenPopularRecordBound = true)
        )

        val logLines = Using(Source.fromInputStream(new FileInputStream(filePath))) { source =>
          source.getLines().toList
        } match {
          case Success(lines) => lines
          case Failure(exception) =>
            println(s"Error reading file: ${exception.getMessage}")
            List.empty
        }

        val updatedReports = logLines.foldLeft(reportList) { (reports, logLine) =>
            val clearLogLine = logLine.trim()
            val logRecord = NginxLogRecord.newLogRecordFromString(clearLogLine)

            if (logRecord.requestTimeStamp.isAfter(fromDate) ||
                logRecord.requestTimeStamp.isEqual(fromDate)) then
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
        
        FileGenerator.createFile("./report_dist", finalReportName, finalReport)