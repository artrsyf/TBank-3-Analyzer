package loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class UserAgentsLogReport(
  private val userAgentCounts: Map[String, Int] = Map.empty
) extends LogReport:

  override def show(): Unit = 
    println(userAgentCounts)

  override def generateMarkdownReport(): String = 
    val reportHeader = s"""
                          |#### User-Agents
                          |
                          || User-Agent              | Количество         |
                          ||:------------------------|-------------------:|""".stripMargin
                        
    val reportBody = userAgentCounts.foldLeft("") { (acc, current) =>
      val (userAgent, count) = current
      acc + s"\n| `$userAgent` | $count |"
    }

    reportHeader + reportBody

  override def generateAsciidocReport(): String = 
    val reportHeader = s"""
                          |== User-Agents
                          |
                          |[cols="2a,1", options="header"]
                          ||===
                          || User-Agent              | Количество         """.stripMargin
    
    val reportBody = userAgentCounts.foldLeft("") { (acc, current) =>
      val (userAgent, count) = current
      acc + s"\n| `$userAgent` | $count"
    } + s"\n|==="

    reportHeader + reportBody

  override def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport = 
    val userAgent = logRecord.userAgent
    val userAgentWithoutVersion = userAgent.split("/").head
    val updatedCount = userAgentCounts.getOrElse(userAgentWithoutVersion, 0) + 1

    copy(
      userAgentCounts = userAgentCounts.updated(userAgentWithoutVersion, updatedCount)
    )
end UserAgentsLogReport