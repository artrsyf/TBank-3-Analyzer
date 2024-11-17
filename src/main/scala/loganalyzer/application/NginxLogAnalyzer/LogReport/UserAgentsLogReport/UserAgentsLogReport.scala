package loganalyzer.application.NginxLogAnalyzer.LogReport.UserAgentsLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class UserAgentsLogReport(
  private val userAgentCounts: Map[String, Int] = Map.empty
) extends LogReport:

  // Выводит содержимое отчёта в консоль
  override def show(): Unit = 
    println(userAgentCounts)

  // Генерация отчёта в формате Markdown
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

  // Генерация отчёта в формате Asciidoc
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

  // Обновление отчёта с новой записью лога
  override def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport = 
    val userAgent = logRecord.userAgent
    val userAgentWithoutVersion = userAgent.split("/").head
    val updatedCount = userAgentCounts.getOrElse(userAgentWithoutVersion, 0) + 1

    copy(
      userAgentCounts = userAgentCounts.updated(userAgentWithoutVersion, updatedCount)
    )
end UserAgentsLogReport