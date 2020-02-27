package com.dowjones.indexer

object IndexerUtils {

  def lineToWords(line: String): Array[String] = {
    line
      .split("[^a-zA-Z']+")
      .filter(_.nonEmpty)
      .map(_.toLowerCase)
  }
}
