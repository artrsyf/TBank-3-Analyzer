package loganalyzer.application.NginxLogAnalyzer.NginxLogRecordFilter

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

class NginxLogRecordFilter:
  def createFilterFunction(
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
