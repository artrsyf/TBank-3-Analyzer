package loganalyzer.application.NginxLogAnalyzer.LogReport.GeneralLogReport

import org.scalatest.funsuite.AnyFunSuite

import loganalyzer.application.NginxLogAnalyzer.NginxLogRecord.NginxLogRecord
import loganalyzer.application.NginxLogAnalyzer.LogReport.LogReport

class GeneralLogReportSpec extends AnyFunSuite:
  test("GeneralLogReport.generateMarkdownReport success"):
    val logs = List(
      "173.29.150.189 - - [13/Nov/2024:17:16:09 +0000] \"GET /logistical.css HTTP/1.1\" 200 1205 \"-\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_0) AppleWebKit/5332 (KHTML, like Gecko) Chrome/38.0.819.0 Mobile Safari/5332\"",
      "73.39.26.244 - - [13/Nov/2024:17:16:09 +0000] \"PUT /bottom-line/pricing%20structure/Future-proofed_contingency/Ergonomic.htm HTTP/1.1\" 200 938 \"-\" \"Mozilla/5.0 (Macintosh; PPC Mac OS X 10_8_3 rv:5.0; en-US) AppleWebKit/532.6.6 (KHTML, like Gecko) Version/4.1 Safari/532.6.6\"",
      "64.29.53.240 - - [13/Nov/2024:17:16:09 +0000] \"PUT /dedicated.php HTTP/1.1\" 200 2549 \"-\" \"Opera/8.50 (Macintosh; PPC Mac OS X 10_9_0; en-US) Presto/2.9.164 Version/11.00\""
    )

    val logRecords = logs.map { log =>
      NginxLogRecord.newLogRecordFromString(log)
    }

    val initReport: LogReport = GeneralLogReport(fileNames = List("test.txt"))

    val updatedLogReport = logRecords.foldLeft(initReport) { (report, record) =>
      report.updateWithSingleIteration(record)
    }

    val markdownReport = updatedLogReport.generateMarkdownReport()
    
    val expected = s"""
                      |                      
                      |
                      |#### Общая информация
                      |
                      || Метрика                 | Значение           |
                      ||:------------------------|-------------------:|
                      || Файл(-ы)                | `test.txt`   |
                      || Начальная дата          | 13.11.2024 |
                      || Конечная дата           | 13.11.2024   |
                      || Количество запросов     | 3       |
                      || Средний размер ответа   | 1564b  |
                      || 95p размера ответа      | 2549b      |
                      |""".stripMargin

    assert(
      markdownReport.trim.replaceAll("\\s+", " ") == expected.trim.replaceAll("\\s+", " "),
      s"Expected: \n$expected\n\nGot: \n$markdownReport"
    )

  test("GeneralLogReport.generateAsciidocReport success"):
    val logs = List(
      "173.29.150.189 - - [13/Nov/2024:17:16:09 +0000] \"GET /logistical.css HTTP/1.1\" 200 1205 \"-\" \"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_0) AppleWebKit/5332 (KHTML, like Gecko) Chrome/38.0.819.0 Mobile Safari/5332\"",
      "73.39.26.244 - - [13/Nov/2024:17:16:09 +0000] \"PUT /bottom-line/pricing%20structure/Future-proofed_contingency/Ergonomic.htm HTTP/1.1\" 200 938 \"-\" \"Mozilla/5.0 (Macintosh; PPC Mac OS X 10_8_3 rv:5.0; en-US) AppleWebKit/532.6.6 (KHTML, like Gecko) Version/4.1 Safari/532.6.6\"",
      "64.29.53.240 - - [13/Nov/2024:17:16:09 +0000] \"PUT /dedicated.php HTTP/1.1\" 200 2549 \"-\" \"Opera/8.50 (Macintosh; PPC Mac OS X 10_9_0; en-US) Presto/2.9.164 Version/11.00\""
    )

    val logRecords = logs.map { log =>
      NginxLogRecord.newLogRecordFromString(log)
    }

    val initReport: LogReport = GeneralLogReport(fileNames = List("test.txt"))

    val updatedLogReport = logRecords.foldLeft(initReport) { (report, record) =>
      report.updateWithSingleIteration(record)
    }

    val markdownReport = updatedLogReport.generateAsciidocReport()

    val expected = s"""
                      |                      
                      |
                      |== Общая информация
                      |
                      |[cols="2a,1", options="header"]
                      ||===
                      || Метрика                 | Значение           
                      || Файл(-ы)                | `test.txt`   
                      || Начальная дата          | 13.11.2024 
                      || Конечная дата           | 13.11.2024   
                      || Количество запросов     | 3       
                      || Средний размер ответа   | 1564b  
                      || 95p размера ответа      | 2549b      
                      ||===
                      |""".stripMargin

    assert(
      markdownReport.trim.replaceAll("\\s+", " ") == expected.trim.replaceAll("\\s+", " "),
      s"Expected: \n$expected\n\nGot: \n$markdownReport"
    )