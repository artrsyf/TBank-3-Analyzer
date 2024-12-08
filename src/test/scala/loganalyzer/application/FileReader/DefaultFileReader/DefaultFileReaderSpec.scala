package loganalyzer.application.FileReader.DefaultFileReader

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite

import java.io.{File, FileOutputStream, InputStream}
import java.nio.file.{Files, Paths}

class DefaultFileReaderSpec extends AnyFunSuite:
  test(
    "DefaultFileReader.readFileNames success. Should return file names matching glob pattern"
  ):
    val existingFileName1 = "test1.txt"
    val existingFileName2 = "test2.txt"
    val pattern = "src/main/resources/test/*.txt"

    val fileReader = DefaultFileReader()

    val result = fileReader.resolveGlobPatternNames(pattern).unsafeRunSync()
    println(result)

    assert(result.contains(existingFileName1))
    assert(result.contains(existingFileName2))

  test(
    "DefaultFileReader.readFileNames success. Should handle multiple paths and URLs"
  ):
    val existingFileName1 = "test1.txt"
    val existingFileName2 = "test2.txt"
    val pattern = "src/main/resources/test/*.txt"
    val pathsOrUrls = s"$pattern, https://example.com/logs"

    val fileReader = DefaultFileReader()

    val result = fileReader.readFileNames(pathsOrUrls).unsafeRunSync()

    assert(result.exists(_.contains(existingFileName1)))
    assert(result.exists(_.contains(existingFileName2)))
    assert(result.contains("https://example.com/logs"))

  test(
    "DefaultFileReader.resolveGlobPatternNames success. Should return an empty list for non-matching pattern"
  ):
    val pattern = "nonexistent/*.log"

    val fileReader = DefaultFileReader()

    val result = fileReader.resolveGlobPatternNames(pattern).unsafeRunSync()

    assert(result.isEmpty)
