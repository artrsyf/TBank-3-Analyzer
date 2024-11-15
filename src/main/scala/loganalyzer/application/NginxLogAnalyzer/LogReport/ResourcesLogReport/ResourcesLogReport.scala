package loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class ResourcesLogReport(
  resourceQueriesNumbers: Map[String, Int] = Map.empty
) extends LogReport:
  override def show(): Unit = 
    println(resourceQueriesNumbers)

  def makeResourcesReport(previousLogReport: ResourcesLogReport, logRecord: NginxLogRecord): ResourcesLogReport = 
    val resourceUrl = logRecord.requestUrl
    val updatedQueriesCount = previousLogReport.resourceQueriesNumbers.getOrElse(resourceUrl, 0) + 1

    previousLogReport.copy(
      resourceQueriesNumbers = previousLogReport.resourceQueriesNumbers.updated(resourceUrl, updatedQueriesCount)
    )