package loganalyzer.application.FileGenerator

import java.nio.file.{Files, Paths}
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object FileGenerator:
  def createFile(directoryPath: String, fileName: String, content: String): Unit =
    // Проверяем, существует ли директория, и создаем её при необходимости
    val dir = Paths.get(directoryPath)
    if !Files.exists(dir) then Files.createDirectories(dir)

    val filePath = Paths.get(directoryPath, fileName)

    // Заменить на Try
    val writer =  new OutputStreamWriter(new FileOutputStream(filePath.toString()), StandardCharsets.UTF_8)
    try writer.write(content)
    finally writer.close()