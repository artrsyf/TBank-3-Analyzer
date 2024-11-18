package loganalyzer.application.FileReader

import scala.jdk.CollectionConverters._

import cats.effect.IO
import cats.syntax.all._ 

import java.io.{FileInputStream, InputStream}
import java.net.URL
import java.nio.file.{Files, FileSystems, Paths}
import java.util.stream.Collectors

object FileReader:

  def readFileNames(pathsOrUrls: String): IO[List[String]] =
    pathsOrUrls.split(",").toList.map(_.trim).traverse { pathOrUrl =>
      if (isUrl(pathOrUrl)) then
        IO.pure(List(pathOrUrl))
      else
        resolveGlobPatternNames(pathOrUrl)
    }.map(_.flatten)

  def readFiles(pathsOrUrls: String): IO[List[InputStream]] =
    pathsOrUrls.split(",").toList.map(_.trim).traverse { pathOrUrl =>
      if isUrl(pathOrUrl) then
        readUrl(pathOrUrl)
      else
        resolveGlobPattern(pathOrUrl)
    }.map(_.flatten)

  def resolveGlobPatternNames(pattern: String): IO[List[String]] =
    IO {
      val basePath = Paths.get(".").toAbsolutePath.normalize

      val pathMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
      val files = Files.walk(basePath)
        .filter(Files.isRegularFile(_))
        .filter(path => pathMatcher.matches(basePath.relativize(path)))
        .collect(Collectors.toList())
        .asScala
        .toList

      files.map(file => file.getFileName.toString)
    }

  private def isUrl(path: String): Boolean =
    path.startsWith("http://") || path.startsWith("https://")

  private def readUrl(url: String): IO[List[InputStream]] =
    IO {
      val connection = new URL(url).openConnection()
      val inputStream = connection.getInputStream

      List(inputStream)
    }.handleErrorWith { e =>
      IO(println(s"Failed to read URL: $url. Error: ${e.getMessage}")) *> IO(List())
    }

  private def resolveGlobPattern(pattern: String): IO[List[InputStream]] =
    IO {
      val basePath = Paths.get(".").toAbsolutePath.normalize

      val pathMatcher = FileSystems.getDefault.getPathMatcher("glob:" + pattern)
      val files = Files.walk(basePath)
        .filter(Files.isRegularFile(_))
        .filter(path => pathMatcher.matches(basePath.relativize(path)))
        .collect(Collectors.toList())
        .asScala
        .toList

      files.map(file => new FileInputStream(file.toFile))
    }