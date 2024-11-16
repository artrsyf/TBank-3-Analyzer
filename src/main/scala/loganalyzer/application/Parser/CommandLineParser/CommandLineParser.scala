package loganalyzer.application.parser.CommandLineParser

import scopt.OParser

case class Config(
  path: String = "",
  from: Option[String] = None,
  format: String = "text"
)

object CommandLineParser {

  def run(args: Array[String]): Either[Error, Config] = {
    // Конфигурация для парсинга аргументов
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
          .text("Путь к логам, поддерживает шаблоны, например: logs/2024*"),

        opt[String]("from")
          .optional()
          .valueName("<date>")
          .action((x, c) => c.copy(from = Some(x)))
          .text("Начальная дата в формате Nginx: DD/MMM/YYYY:HH:MM:SS Z"),

        opt[String]("format")
          .optional()
          .valueName("<format>")
          .action((x, c) => c.copy(format = x))
          .text("Формат вывода: adoc или markdown")
      )
    }

    // Парсинг аргументов и запуск программы
    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        // Если парсинг успешен, запускаем анализ логов
        Right(config)

      case _ =>
        Left(Error("Mismatch"))
        // Если парсинг неуспешен, scopt автоматически выведет сообщение об ошибке
    }
  }
}