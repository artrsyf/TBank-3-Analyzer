package loganalyzer.application.FileGenerator

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import scala.io.Source
import cats.effect.unsafe.implicits.global   

class FileGeneratorSpec extends AnyFunSuite:
  private def createTempDirectory(): java.nio.file.Path =
    val tempDir = Files.createTempDirectory("filegenerator-test")
    tempDir.toFile.deleteOnExit()
    tempDir

  test("FileGenerator.createFile success. Should create a file with the specified content"):
    val tempDir = createTempDirectory()
    val directoryPath = tempDir.toString
    val fileName = "testfile.txt"
    val content = "This is a test file."

    FileGenerator.createFile(directoryPath, fileName, content).unsafeRunSync()

    val filePath = Paths.get(directoryPath, fileName)

    assert(Files.exists(filePath))

    val fileContent = Source.fromFile(filePath.toFile).getLines().mkString("\n")
    assert(fileContent == content)

  test("FileGenerator.createFile success. Should create directory if it does not exist"):
    val tempDir = createTempDirectory()
    val nestedDir = Paths.get(tempDir.toString, "nested").toString
    val fileName = "nestedfile.txt"
    val content = "This is a file in a nested directory."

    FileGenerator.createFile(nestedDir, fileName, content).unsafeRunSync()

    val filePath = Paths.get(nestedDir, fileName)

    assert(Files.exists(Paths.get(nestedDir)))
    assert(Files.exists(filePath))

    val fileContent = Source.fromFile(filePath.toFile).getLines().mkString("\n")
    assert(fileContent == content)