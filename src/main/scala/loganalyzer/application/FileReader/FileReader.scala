package loganalyzer.application.FileReader

import cats.effect.IO
import cats.syntax.all._

import java.io.InputStream

trait FileReader:
  def readFileNames(pathsOrUrls: String): IO[List[String]]
  def readFiles(pathsOrUrls: String): IO[List[InputStream]]
