package loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class ResourcesLogReport(
  private val resourceQueriesNumbers: Map[String, Int] = Map.empty
) extends LogReport:

  override def show(): Unit = 
    println(resourceQueriesNumbers)

  def updateWithSingleIteration(logRecord: NginxLogRecord): ResourcesLogReport = 
    val resourceUrl = logRecord.requestUrl
    val updatedQueriesCount = resourceQueriesNumbers.getOrElse(resourceUrl, 0) + 1

    copy(
      resourceQueriesNumbers = resourceQueriesNumbers.updated(resourceUrl, updatedQueriesCount)
    )
end ResourcesLogReport