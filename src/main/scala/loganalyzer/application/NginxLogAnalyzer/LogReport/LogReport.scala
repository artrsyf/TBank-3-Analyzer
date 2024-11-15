package loganalyzer.application.NginxLogAnalyzer.LogReport

trait LogReport:
  def show(): Unit
  def generateMarkdownReport(): Unit
  def generateAsciidocReport(): Unit