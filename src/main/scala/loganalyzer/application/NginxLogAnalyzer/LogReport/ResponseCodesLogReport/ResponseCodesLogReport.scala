package loganalyzer.application.NginxLogAnalyzer.LogReport.ResponseCodesLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord
import loganalyzer.shared.constants.Http.mapResponseCodeToName

case class ResponseCodesLogReport(
  private val responseCodes: Map[(Int, String), Int] = Map.empty
) extends LogReport:

  override def show(): Unit = 
    println(responseCodes)

  def updateWithSingleIteration(logRecord: NginxLogRecord): ResponseCodesLogReport = 
    val responseCodeName = mapResponseCodeToName(logRecord.responseCode)
    val responseResult = (logRecord.responseCode, responseCodeName)
    val updatedResponsesCount = responseCodes.getOrElse(responseResult, 0) + 1

    copy(
      responseCodes = responseCodes.updated(responseResult, updatedResponsesCount)
    )
end ResponseCodesLogReport