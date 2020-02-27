package com.dowjones

import com.dowjones.indexer.IndexerUtils._
import com.spotify.scio._
import com.spotify.scio.values.SCollection

object Dictionary {

  def main(cmdlineArgs: Array[String]): Unit = {
    val (sc, args) = ContextAndArgs(cmdlineArgs)

    val path = "data/documents/*"
    val input = args.getOrElse("input", path)
    val output = args.getOrElse("output", "dictionary")

    createDictionary(sc.textFile(input), { a: String =>
      a.hashCode
    }).saveAsTextFile(output, 1)

    sc.run().waitUntilFinish()
    ()
  }

  private def createDictionary[A](input: SCollection[String], encoder: String => A) = {
    input
      .map(_.trim)
      .flatMap(lineToWords)
      .distinct
      .map(a => s"$a ${encoder(a)}")
  }
}
