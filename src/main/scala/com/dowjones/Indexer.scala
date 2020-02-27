package com.dowjones

import java.nio.channels.Channels

import com.dowjones.indexer.IndexerUtils._
import com.spotify.scio._
import com.spotify.scio.values.SCollection
import org.apache.beam.sdk.io.FileSystems
import scala.collection.JavaConverters._
import scala.collection.SortedSet
import scala.io.Source

object Indexer {

  def main(cmdlineArgs: Array[String]): Unit = {
    val (sc, args) = ContextAndArgs(cmdlineArgs)

    val defaultDocumentPath = "data/documents/*"
    val defaultDictionaryPath = "data/dictionary/part-00000-of-00001.txt"
    val output = args.getOrElse("output", "index")
    val dictionaryPath = args.getOrElse("dictionary", defaultDictionaryPath)

    val dictionary = loadDictionary(dictionaryPath, sc)
    val inputs = wordsWithFileName(args.getOrElse("input", defaultDocumentPath), sc)

    mergeWithDictionary(inputs, dictionary)
      .saveAsTextFile(output, 1)

    sc.run().waitUntilFinish()
    ()
  }

  private def loadDictionary(path: String, sc: ScioContext): SCollection[(String, Int)] = {
    sc.textFile(path)
      .map { line =>
        val pair = line.split(" ")
        pair(0) -> pair(1).toInt // TODO error handling - toInt, indices
      }
  }

  private def mergeWithDictionary(words: SCollection[(String, String)], dictionaryItems: SCollection[(String, Int)]) = {
    words
      .join(dictionaryItems)
      .map { case (_, (docId, wordId)) => wordId -> docId }
      .distinct
      .aggregateByKey(SortedSet[String]())(_ + _, _ ++ _)
      .map(e => s"${e._1} [${e._2.mkString(",")}]")
  }

  private def wordsWithFileName(inputPath: String, sc: ScioContext) = {
    val uris = FileSystems
      .`match`(inputPath)
      .metadata()
      .asScala
      .map(_.resourceId().toString)

    sc.parallelize(uris)
      .flatMap { uri =>
        val resourceId = FileSystems.matchSingleFileSpec(uri).resourceId()
        val inputStream = Channels.newInputStream(FileSystems.open(resourceId))

        Source
          .fromInputStream(inputStream)
          .getLines()
          .flatMap { line =>
            lineToWords(line).map(word => word -> fileName(uri))
          }
      }
  }

  private def fileName(uri: String): String = {
    uri.split("/").last
  }
}
