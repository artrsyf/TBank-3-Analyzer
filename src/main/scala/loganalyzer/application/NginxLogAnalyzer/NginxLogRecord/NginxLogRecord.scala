package loganalyzer.application.NginxLogAnalyzer.NginxLogRecord

import java.time.OffsetDateTime

import loganalyzer.shared.configs.NginxLogFormatter

import loganalyzer.shared.enums.Http.RequestMethod

case class NginxLogRecord(
  remoteAddress: String,
  requestTimeStamp: OffsetDateTime,
  requestMethod: RequestMethod,
  requestUrl: String,
  httpVersion: String,
  responseCode: Int,
  responseSize: Int,
  referer: String,
  userAgent: String
)

object NginxLogRecord:

  def apply(logLine: String): NginxLogRecord =
    val fields = logLine.split(" ")
    val dateString = fields(3).replaceAll("[\\[\\]\"]", "") + " " + fields(4)
      .replaceAll("[\\[\\]\"]", "")

    NginxLogRecord(
      remoteAddress = fields(0).replaceAll("[\\[\\]\"]", ""),
      requestTimeStamp = parseNginxDate(dateString),
      requestMethod =
        RequestMethod.valueOf(fields(5).replaceAll("[\\[\\]\"]", "")),
      requestUrl = fields(6).replaceAll("[\\[\\]\"]", ""),
      httpVersion = fields(7).replaceAll("[\\[\\]\"]", ""),
      responseCode = fields(8).replaceAll("[\\[\\]\"]", "").toInt,
      responseSize = fields(9).replaceAll("[\\[\\]\"]", "").toInt,
      referer = fields(10).replaceAll("[\\[\\]\"]", ""),
      userAgent = fields(11).replaceAll("[\\[\\]\"]", "")
    )

  private def parseNginxDate(logDate: String): OffsetDateTime =
    val clearedLogDate = logDate.trim()

    OffsetDateTime.parse(clearedLogDate, NginxLogFormatter)
end NginxLogRecord
