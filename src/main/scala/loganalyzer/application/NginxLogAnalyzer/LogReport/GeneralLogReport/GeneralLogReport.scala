package loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class GeneralLogReport(
  private val fileNames: List[String] = List(),
  private val startDate: OffsetDateTime = OffsetDateTime.MAX,
  private val endDate: OffsetDateTime = OffsetDateTime.MIN,
  private val queryNumber: Int = 0,
  private val responseSizes: List[Int] = List.empty
) extends LogReport:

  override def show(): Unit =
    println(s"Files: ${fileNames.mkString(", ")}")

  override def generateMarkdownReport(): String =
    s"""
       |#### Общая информация
       |
       || Метрика                 | Значение           |
       ||:------------------------|-------------------:|
       || Файл(-ы)                | ${fileNames.mkString("`", "`, `", "`")}   |
       || Начальная дата          | ${formatDate(startDate)} |
       || Конечная дата           | ${formatDate(endDate)}   |
       || Количество запросов     | $queryNumber       |
       || Средний размер ответа   | ${averageResponseSize}b  |
       || 95p размера ответа      | ${responseSize95Percentile}b      |
       |""".stripMargin

  override def generateAsciidocReport(): String =
    s"""
       |== Общая информация
       |
       |[cols="2a,1", options="header"]
       ||===
       || Метрика                 | Значение
       || Файл(-ы)                | ${fileNames.mkString("`", "`, `", "`")}
       || Начальная дата          | ${formatDate(startDate)}
       || Конечная дата           | ${formatDate(endDate)}
       || Количество запросов     | $queryNumber
       || Средний размер ответа   | ${averageResponseSize}b
       || 95p размера ответа      | ${responseSize95Percentile}b
       ||===
       |""".stripMargin

  override def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport =
    val updatedEndDate =
      if (logRecord.requestTimeStamp.isAfter(endDate)) then
        logRecord.requestTimeStamp
      else endDate

    val updatedStartDate =
      if (logRecord.requestTimeStamp.isBefore(startDate)) then
        logRecord.requestTimeStamp
      else endDate

    copy(
      endDate = updatedEndDate,
      startDate = updatedStartDate,
      queryNumber = queryNumber + 1,
      responseSizes = responseSizes :+ logRecord.responseSize
    )

  private def formatDate(date: OffsetDateTime): String =
    if date == OffsetDateTime.MAX || date == OffsetDateTime.MIN then "-"
    else date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  private def responseSize95Percentile: Int =
    if responseSizes.nonEmpty then
      val sortedSizes = responseSizes.sorted
      val index = math.ceil(sortedSizes.size * 0.95).toInt - 1
      sortedSizes(index)
    else 0

  private def averageResponseSize: Int =
    if (responseSizes.nonEmpty) {
      val sum = responseSizes.map(_.toLong).sum // Суммируем значения как Long
      (sum / responseSizes.size).toInt // Возвращаем результат как Int
    } else 0
end GeneralLogReport
