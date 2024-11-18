import cats.effect.{IO, IOApp, ExitCode}

import scala.io.Source
import scala.util.Using
import scala.util.{Try, Success, Failure}

import java.io.{FileInputStream, InputStream}
import java.net.URL
import java.nio.file.{Files, FileSystems, Paths}
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._
import cats.syntax.all._ 

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

object FileReader:

  def readFileNames(pathsOrUrls: String): IO[List[String]] =
    // Разделяем строки путей по запятой и обрабатываем каждую отдельно
    pathsOrUrls.split(",").toList.map(_.trim).traverse { pathOrUrl =>
      if (isUrl(pathOrUrl)) then
        IO.pure(List(pathOrUrl)) // Просто возвращаем URL как строку
      else
        resolveGlobPatternNames(pathOrUrl)
    }.map(_.flatten) // Уплощаем список списков

  def readFiles(pathsOrUrls: String): IO[List[InputStream]] =
    // Разделяем строки путей по запятой и обрабатываем каждую отдельно
    pathsOrUrls.split(",").toList.map(_.trim).traverse { pathOrUrl =>
      if isUrl(pathOrUrl) then
        readUrl(pathOrUrl)
      else
        resolveGlobPattern(pathOrUrl)
    }.map(_.flatten) // Уплощаем список списков

  def resolveGlobPatternNames(pattern: String): IO[List[String]] =
    IO {
      val basePath = Paths.get(".").toAbsolutePath.normalize // Определяем рабочую директорию

      val pathMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
      val files = Files.walk(basePath) // Используем текущую директорию как корневую
        .filter(Files.isRegularFile(_))
        .filter(path => pathMatcher.matches(basePath.relativize(path))) // Сравниваем с учетом базового пути
        .collect(Collectors.toList())
        .asScala
        .toList

      files.map(file => file.getFileName.toString) // Возвращаем полные пути файлов как строки
    }

  private def isUrl(path: String): Boolean =
    path.startsWith("http://") || path.startsWith("https://")

  private def readUrl(url: String): IO[List[InputStream]] =
    IO {
      val connection = new URL(url).openConnection()
      val inputStream = connection.getInputStream
      // Возвращаем InputStream в списке, так как ожидается список в IO
      List(inputStream)
    }.handleErrorWith { e =>
      IO(println(s"Failed to read URL: $url. Error: ${e.getMessage}")) *> IO(List())
    }

  private def resolveGlobPattern(pattern: String): IO[List[InputStream]] =
    IO {
      val basePath = Paths.get(".").toAbsolutePath.normalize // Определяем рабочую директорию

      val pathMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
      val files = Files.walk(basePath) // Используем текущую директорию как корневую
        .filter(Files.isRegularFile(_))
        .filter(path => pathMatcher.matches(basePath.relativize(path))) // Сравниваем с учетом базового пути
        .collect(Collectors.toList())
        .asScala
        .toList

      files.map(file => new FileInputStream(file.toFile))
    }

object Main extends IOApp:

  def run(args: List[String]) =
    CommandLineParser.run(args).flatMap {
      case Left(error) =>
        IO(println("Error parsing command line arguments")) *> IO(ExitCode.Error)
      case Right(config) =>
        val filePath = config.path
        val reportFormat = config.format
        val formatterIso8601 = DateTimeFormatter
          .ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH)

        val fromDate = config.from match
          case Some(value) =>
            OffsetDateTime.parse(value, formatterIso8601)
          case _ =>
            OffsetDateTime.MIN

        val toDate = config.to match
          case Some(value) =>
            OffsetDateTime.parse(value, formatterIso8601)
          case _ =>
            OffsetDateTime.MAX

        val filterFunction: NginxLogRecord => Boolean =
          createFilterFunction(config.filterField, config.filterValue)

        val reportList = Array(
          GeneralLogReport(),
          ResponseCodesLogReport(),
          UserAgentsLogReport(),
          HttpMethodsLogReport(),
          ResourcesLogReport(tenPopularRecordBound = true)
        )

        val logLines: IO[List[String]] = 
          FileReader.readFiles(filePath).flatMap { inputStreams =>
            IO {
              inputStreams.flatMap { inputStream =>
                Using(Source.fromInputStream(inputStream)) { source =>
                  source.getLines().toList
                }.getOrElse(List.empty[String])
              }
            }
          }.handleErrorWith { e =>
            IO {
              println(s"Error reading file(s): ${e.getMessage}")
              List.empty[String]
            }
          }

        logLines.flatMap { lines =>
          FileReader.readFileNames(filePath).flatMap { fileNames =>
            val updatedReports = 
              reportList.map(report =>
                report match
                  case generalReport: GeneralLogReport =>
                    generalReport.copy(fileNames = fileNames) // Обновляем имена файлов
                  case otherReport => otherReport
              )

            val updatedReportsAfterProcessing =
              lines.foldLeft(updatedReports) { (reports, logLine) =>
                val clearLogLine = logLine.trim()
                val logRecord = NginxLogRecord.newLogRecordFromString(clearLogLine)

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
                ) then
                  reports.map(report => report.updateWithSingleIteration(logRecord))
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

            FileGenerator.createFile("./report_dist", finalReportName, finalReport) *>
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
