package loganalyzer.application.NginxLogAnalyzer.LogReport

trait LogReport:
  def show(): Unit
  def generateMarkdownReport(): String
  def generateAsciidocReport(): String