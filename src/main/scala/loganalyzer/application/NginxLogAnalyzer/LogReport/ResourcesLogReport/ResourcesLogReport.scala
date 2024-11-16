package loganalyzer.application.NginxLogAnalyzer.LogReport.ResourcesLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class ResourcesLogReport(
  private val resourceQueriesNumbers: Map[String, Int] = Map.empty
) extends LogReport:

  override def show(): Unit = 
    println(resourceQueriesNumbers)

  override def generateMarkdownReport(): String = 
    val reportHeader = s"""
                          |#### Запрашиваемые ресурсы
                          |
                          || Ресурс                  | Количество         |
                          ||:------------------------|-------------------:|""".stripMargin
                        
    val reportBody = resourceQueriesNumbers.foldLeft("") { (acc, current) =>
      val (resourcePath, queryNumber) = current
      acc + s"\n| `$resourcePath` | $queryNumber |"
    }

    reportHeader + reportBody

  override def generateAsciidocReport(): String = 
    val reportHeader = s"""
                          |== Общая информация
                          |
                          |[cols="2a,1", options="header"]
                          ||===""".stripMargin
    
    val reportBody = resourceQueriesNumbers.foldLeft("") { (acc, current) =>
      val (resourcePath, queryNumber) = current
      acc + s"\n| `$resourcePath` | $queryNumber"
    } + s"\n|==="

    reportHeader + reportBody

  override def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport = 
    val resourceUrl = logRecord.requestUrl
    val updatedQueriesCount = resourceQueriesNumbers.getOrElse(resourceUrl, 0) + 1

    copy(
      resourceQueriesNumbers = resourceQueriesNumbers.updated(resourceUrl, updatedQueriesCount)
    )
end ResourcesLogReport