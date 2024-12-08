package loganalyzer.application.FileGenerator

import cats.effect.IO

trait FileGenerator:

  def createFile(
    directoryPath: String,
    fileName: String,
    content: String
  ): IO[Unit]
