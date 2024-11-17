package loganalyzer.application.parser.CommandLineParser

import cats.effect.IO
import scopt.OParser

case class Config(
  path: String = "",
  from: Option[String] = None,
  format: String = "text",
  filterField: Option[String] = None,
  filterValue: Option[String] = None
)

object CommandLineParser:

  def run(args: List[String]): IO[Either[Error, Config]] = IO {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._

      OParser.sequence(
        programName("analyzer"),
        head("Analyzer", "1.0"),
        opt[String]("path")
          .required()
          .valueName("<path>")
          .action((x, c) => c.copy(path = x))
          .text(
            "Путь к логам (Обязательно в кодировке UTF-8), например: ./logs.txt"
          ),
        opt[String]("from")
          .optional()
          .valueName("<date>")
          .action((x, c) => c.copy(from = Some(x)))
          .text("Начальная дата в формате Nginx: DD/MMM/YYYY:HH:MM:SS Z"),
        opt[String]("format")
          .optional()
          .valueName("<format>")
          .action((x, c) => c.copy(format = x))
          .text("Формат вывода: adoc или markdown"),
        opt[String]("filter-field")
          .optional()
          .valueName("<field>")
          .action((x, c) => c.copy(filterField = Some(x)))
          .text(
            """Имя поля для фильтрации. Поддерживаются следующие значения:
              |
              | - `address`        : Фильтрация по IP-адресу клиента. Пример значения: `192.168.1.1`
              | - `method`         : Фильтрация по HTTP-методу запроса (например, `GET`, `POST`).
              | - `url`            : Фильтрация по URL запрашиваемого ресурса. Частичное совпадение, пример значения: `/index.html`.
              | - `protocol`       : Фильтрация по версии протокола HTTP (например, `HTTP/1.1`, `HTTP/2.0`).
              | - `response-code`  : Фильтрация по HTTP-коду ответа сервера. Пример значения: `404`, `200`.
              | - `response-size`  : Фильтрация по размеру ответа сервера (в байтах). Пример значения: `1024`.
              | - `referer`        : Фильтрация по рефереру (поле `Referer`). Пример значения: `https://google.com`.
              | - `user-agent`     : Фильтрация по строке User-Agent клиента. Частичное совпадение, пример значения: `Mozilla/5.0`.
              |
              |Значение задаётся в параметре `--filter-value`. Например:
              |  --filter-field method --filter-value "GET"
              |""".stripMargin
          ),
        opt[String]("filter-value")
          .optional()
          .valueName("<value>")
          .action((x, c) => c.copy(filterValue = Some(x)))
          .text("Значение фильтра, например: GET или /index.html")
      )
    }

    OParser.parse(parser, args, Config()) match
      case Some(config) =>
        Right(config)
      case _ =>
        Left(Error("Argument mismatch"))
  }
