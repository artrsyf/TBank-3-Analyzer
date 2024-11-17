package loganalyzer.application.NginxLogAnalyzer.LogReport.HttpMethodsLogReport

import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport
import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

case class HttpMethodsLogReport(
  private val httpMethodCounts: Map[String, Int] = Map.empty
) extends LogReport:

  // Выводит содержимое отчёта в консоль
  override def show(): Unit = 
    println(httpMethodCounts)

  // Генерация отчёта в формате Markdown
  override def generateMarkdownReport(): String = 
    val reportHeader = s"""
                          |#### HTTP-методы
                          |
                          || Метод       | Количество         |
                          ||:------------|-------------------:|""".stripMargin
                        
    val reportBody = httpMethodCounts.foldLeft("") { (acc, current) =>
      val (method, count) = current
      acc + s"\n| `$method` | $count |"
    }

    reportHeader + reportBody

  // Генерация отчёта в формате Asciidoc
  override def generateAsciidocReport(): String = 
    val reportHeader = s"""
                          |== HTTP-методы
                          |
                          |[cols="2a,1", options="header"]
                          ||===
                          || Метод       | Количество         """.stripMargin
    
    val reportBody = httpMethodCounts.foldLeft("") { (acc, current) =>
      val (method, count) = current
      acc + s"\n| `$method` | $count"
    } + s"\n|==="

    reportHeader + reportBody

  // Обновление отчёта с новой записью лога
  override def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport = 
    val httpMethod = logRecord.requestMethod.toString
    val updatedCount = httpMethodCounts.getOrElse(httpMethod, 0) + 1

    copy(
      httpMethodCounts = httpMethodCounts.updated(httpMethod, updatedCount)
    )
end HttpMethodsLogReport