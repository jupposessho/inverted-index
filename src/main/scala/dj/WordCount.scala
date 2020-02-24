package dj

import java.nio.channels.Channels

import com.spotify.scio._
import com.spotify.scio.values.SCollection
import org.apache.beam.sdk.io.FileSystems
import org.apache.beam.sdk.io.fs.EmptyMatchTreatment
import org.apache.beam.sdk.io.fs.MatchResult.Metadata

import scala.io.Source
import scala.collection.JavaConverters._

/*
sbt "runMain [PACKAGE].WordCount
  --project=[PROJECT] --runner=DataflowRunner --zone=[ZONE]
  --input=gs://dataflow-samples/shakespeare/kinglear.txt
  --output=gs://[BUCKET]/[PATH]/wordcount"
*/

object WordCount {

  final case class DictionaryItem(id: Int, word: String)

  def main(cmdlineArgs: Array[String]): Unit = {
    val (sc, args) = ContextAndArgs(cmdlineArgs)

    val path = "data/dj2/*"
    val dictionaryPath = "data/dictionary/part-00000-of-00001.txt"
    val input = args.getOrElse("input", path)
    val output = args("output")

    val  dictionary = loadDictionary(dictionaryPath, sc)
    val inputs = wordsWithFileName(args.getOrElse("input", path), sc)

      mergeWithDictionary(inputs, dictionary)
//    inputs
//    dictionary
      .map(println)
      .saveAsTextFile(output, 1)

    ///////////////////////////////////////////
//    createDictionary(sc.textFile(input))
//      .saveAsTextFile("dictionary", 1)

    sc.run().waitUntilFinish()
    ()
  }

  private def loadDictionary(path: String, sc: ScioContext): SCollection[DictionaryItem] = {
    sc.textFile(path)
      .map { line =>
        val pair = line.split(" ")
        DictionaryItem(pair(1).toInt, pair(0)) // TODO error handling - toInt, indices
      }
  }

  private def mergeWithDictionary(words: SCollection[(String, String)], dictionaryItems: SCollection[DictionaryItem]) = {
    dictionaryItems
  }

  private def createDictionary(input: SCollection[String]) = {
    input
      .map(_.trim)
      .flatMap(lineToWords)
      .distinct
      .map(a => s"$a ${a.hashCode}")
  }

  private def lineToWords(line: String): Array[String] = {
    line
      .split("[^a-zA-Z']+")
      .filter(_.nonEmpty)
      .map(_.toLowerCase)
  }

  private def wordsWithFileName(inputPath: String, sc: ScioContext) = {
    val uris = FileSystems
      .`match`(inputPath)
      .metadata()
      .asScala
      .map(_.resourceId().toString)

      sc
        .parallelize(uris)
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
