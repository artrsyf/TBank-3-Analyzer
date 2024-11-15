package loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class GeneralLogReport(
  private val fileName: String,
  startDate: OffsetDateTime = OffsetDateTime.MAX,
  private val endDate: OffsetDateTime = OffsetDateTime.MIN,
  private val queryNumber: Int = 0,
  private val averageResponseSize: Int = 0
  // TODO: Есть еще какое-топ последнее поле
) extends LogReport:

  override def show(): Unit = 
    println(fileName)

  override def generateMarkdownReport(): String = 
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

  override def generateAsciidocReport(): String = 
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

  def updateWithSingleIteration(logRecord: NginxLogRecord): GeneralLogReport = 
      val updatedEndDate = if (logRecord.requestTimeStamp.isAfter(endDate)) then 
        logRecord.requestTimeStamp 
      else 
        endDate

      val updatedStartDate = if (logRecord.requestTimeStamp.isBefore(startDate)) then 
        logRecord.requestTimeStamp 
      else 
        endDate

      val updatedAverageResponseSize = (
        queryNumber * averageResponseSize
        + logRecord.responseSize) / (queryNumber + 1)
      
      copy(
        endDate = updatedEndDate,
        startDate = updatedStartDate,
        queryNumber = queryNumber + 1,
        averageResponseSize = updatedAverageResponseSize
      )
    
  private def formatDate(date: OffsetDateTime): String =
    if date == OffsetDateTime.MAX || date == OffsetDateTime.MIN then
      "-"
    else
      date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
end GeneralLogReport