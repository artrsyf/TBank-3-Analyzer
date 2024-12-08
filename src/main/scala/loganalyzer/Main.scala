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

import loganalyzer.application.FileGenerator.DefaultFileGenerator.DefaultFileGenerator
import loganalyzer.application.FileReader.DefaultFileReader.DefaultFileReader
import loganalyzer.application.parser.CommandLineParser.CommandLineParser
import loganalyzer.application.NginxLogProcessor.NginxLogProcessor

import loganalyzer.shared.constants.Report.*

object Main extends IOApp:

  def run(args: List[String]) =
    CommandLineParser.run(args).flatMap {
      case Left(error) =>
        IO(println("Error parsing command line arguments")) *> IO(
          ExitCode.Error
        )
      case Right(config) =>
        val fileReader = DefaultFileReader()
        val fileGenerator = DefaultFileGenerator()

        val filter = NginxLogRecordFilter()
        val filterFunction = filter
          .createFilterFunction(config.filterField, config.filterValue)

        val logProcessor = NginxLogProcessor(
          config,
          fileReader,
          fileGenerator,
          filterFunction
        )

        logProcessor.generateReport *> IO(ExitCode.Success)
    }