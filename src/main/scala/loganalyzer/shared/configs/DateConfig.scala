package loganalyzer.shared.configs

import java.time.format.DateTimeFormatter
import java.util.Locale

val FormatterIso8601 = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH)

val NginxLogFormatter = DateTimeFormatter
        .ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)

val ReportFormatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy")