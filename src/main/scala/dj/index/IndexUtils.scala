package dj.index

object IndexUtils {

  def lineToWords(line: String): Array[String] = {
    line
      .split("[^a-zA-Z']+")
      .filter(_.nonEmpty)
      .map(_.toLowerCase)
  }
}
