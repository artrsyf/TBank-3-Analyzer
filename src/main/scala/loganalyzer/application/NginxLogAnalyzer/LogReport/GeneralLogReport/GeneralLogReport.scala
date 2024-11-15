package loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class GeneralLogReport(
  fileName: String,
  startDate: OffsetDateTime = OffsetDateTime.MAX,
  endDate: OffsetDateTime = OffsetDateTime.MIN,
  queryNumber: Int = 0,
  averageResponseSize: Int = 0
  // TODO: Есть еще какое-топ последнее поле
) extends LogReport:
  override def show(): Unit = 
    val report = fileName
    println(report)

  private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  def generateMarkdownReport(): String = 
    s"""
       |#### Общая информация
       |
       || Метрика                 | Значение           |
       ||:------------------------|-------------------:|
       || Файл(-ы)                | `$fileName`        |
       || Начальная дата          | ${formatDate(startDate)} |
       || Конечная дата           | ${formatDate(endDate)}   |
       || Количество запросов     | $queryNumber       |
       || Средний размер ответа   | ${averageResponseSize}b  |
       || 95p размера ответа      | ${"not implemented"}b      |
       |""".stripMargin

  def generateAsciidocReport(): String = 
    s"""
       |==== Общая информация
       |
       |[cols="2a,1", options="header"]
       ||===
       || Метрика                 | Значение
       || Файл(-ы)                | `$fileName`
       || Начальная дата          | ${formatDate(startDate)}
       || Конечная дата           | ${formatDate(endDate)}
       || Количество запросов     | $queryNumber
       || Средний размер ответа   | ${averageResponseSize}b
       || 95p размера ответа      | ${"not implemented"}b
       ||===
       |""".stripMargin

  private def formatDate(date: OffsetDateTime): String =
    if date == OffsetDateTime.MAX || date == OffsetDateTime.MIN then "-"
    else date.format(dateFormatter)

def makeGeneralReport(previousLogReport: GeneralLogReport, logRecord: NginxLogRecord): GeneralLogReport = 
    val updatedEndDate = if (logRecord.requestTimeStamp.isAfter(previousLogReport.endDate)) then 
      logRecord.requestTimeStamp 
    else 
      previousLogReport.endDate

    val updatedStartDate = if (logRecord.requestTimeStamp.isBefore(previousLogReport.startDate)) then 
      logRecord.requestTimeStamp 
    else 
      previousLogReport.endDate

    val updatedAverageResponseSize = (
      previousLogReport.queryNumber * previousLogReport.averageResponseSize
      + logRecord.responseSize) / (previousLogReport.queryNumber + 1
    )
    
    previousLogReport.copy(
      endDate = updatedEndDate,
      startDate = updatedStartDate,
      queryNumber = previousLogReport.queryNumber + 1,
      averageResponseSize = updatedAverageResponseSize
    )