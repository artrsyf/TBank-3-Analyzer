package loganalyzer.application.FileReader

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite

import java.io.{File, FileOutputStream, InputStream}
import java.nio.file.{Files, Paths}

class FileReaderSpec extends AnyFunSuite:
  test(
    "FileReader.readFileNames success. Should return file names matching glob pattern"
  ):
    val existingFileName1 = "test1.txt"
    val existingFileName2 = "test2.txt"
    val pattern = "src/main/resources/test/*.txt"

    val result = FileReader.resolveGlobPatternNames(pattern).unsafeRunSync()
    println(result)

    assert(result.contains(existingFileName1))
    assert(result.contains(existingFileName2))

  test(
    "FileReader.readFileNames success. Should handle multiple paths and URLs"
  ):
    val existingFileName1 = "test1.txt"
    val existingFileName2 = "test2.txt"
    val pattern = "src/main/resources/test/*.txt"

    val pathsOrUrls = s"$pattern, https://example.com/logs"
    val result = FileReader.readFileNames(pathsOrUrls).unsafeRunSync()

    assert(result.exists(_.contains(existingFileName1)))
    assert(result.exists(_.contains(existingFileName2)))
    assert(result.contains("https://example.com/logs"))

  test(
    "FileReader.resolveGlobPatternNames success. Should return an empty list for non-matching pattern"
  ):
    val pattern = "nonexistent/*.log"
    val result = FileReader.resolveGlobPatternNames(pattern).unsafeRunSync()

    assert(result.isEmpty)
