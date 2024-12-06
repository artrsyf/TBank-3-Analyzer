package loganalyzer.application.NginxLogAnalyzer.LogReport

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord

trait LogReport:
  def updateWithSingleIteration(logRecord: NginxLogRecord): LogReport
  def generateMarkdownReport(): String
  def generateAsciidocReport(): String
