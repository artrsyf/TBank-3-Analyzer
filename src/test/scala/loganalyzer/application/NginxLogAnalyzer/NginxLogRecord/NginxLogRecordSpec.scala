package loganalyzer.application.NginxLogAnalyzer.NginxLogRecord

import org.scalatest.funsuite.AnyFunSuite

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import loganalyzer.shared.enums.Http.RequestMethod

class NginxLogRecordSpec extends AnyFunSuite:
  test("NginxLogRecord.newLogRecordFromString success"):
    val nginxLog = "117.34.196.86 - - [13/Nov/2024:17:16:08 +0000] \"GET /installation%20Extended/frame/bifurcated-Down-sized.hmtl HTTP/1.1\" 200 2067 \"-\" \"Mozilla/5.0 (Windows 98) AppleWebKit/5360 (KHTML, like Gecko) Chrome/40.0.867.0 Mobile Safari/5360\""

    val record = NginxLogRecord.newLogRecordFromString(nginxLog)

    assert(record.remoteAddress == "117.34.196.86")

    val expectedDate = OffsetDateTime.parse("13/Nov/2024:17:16:08 +0000", DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH))
    assert(record.requestTimeStamp == expectedDate)

    assert(record.requestMethod == RequestMethod.GET)

    assert(record.requestUrl == "/installation%20Extended/frame/bifurcated-Down-sized.hmtl")

    assert(record.httpVersion == "HTTP/1.1")

    assert(record.responseCode == 200)

    assert(record.responseSize == 2067)

    assert(record.referer == "-")

    assert(record.userAgent == "Mozilla/5.0")