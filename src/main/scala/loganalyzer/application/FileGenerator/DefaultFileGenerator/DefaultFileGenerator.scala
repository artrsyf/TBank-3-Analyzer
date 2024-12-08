package loganalyzer.application.FileGenerator.DefaultFileGenerator

import cats.effect.IO

import java.nio.file.{Files, Paths}
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

import loganalyzer.application.FileGenerator.FileGenerator

class DefaultFileGenerator extends FileGenerator:

  override def createFile(
    directoryPath: String,
    fileName: String,
    content: String
  ): IO[Unit] =
    val dir = Paths.get(directoryPath)

    IO.delay {
      if !Files.exists(dir) then Files.createDirectories(dir)
    }.flatMap { _ =>
      val filePath = Paths.get(directoryPath, fileName)

      IO.delay {
        val writer = new OutputStreamWriter(
          new FileOutputStream(filePath.toString()),
          StandardCharsets.UTF_8
        )
        try
          writer.write(content)
        finally
          writer.close()
      }
    }
