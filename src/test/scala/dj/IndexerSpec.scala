package dj

import com.spotify.scio.io.TextIO
import com.spotify.scio.testing._

class IndexerSpec extends PipelineSpec {

  val inData = Seq("hello world\n", "what's up World?\n")
  val dictionaryData = Seq("hello 99162322", "world 113318802", "what's -789049616", "up 3739")
  val expected = Seq()

  "Indexer" should "create index from data and dictionary" in {
    JobTest[dj.Indexer.type]
      .args("--input=in.txt", "--dictionary=dictionary.txt", "--output=out.txt")
      .input(TextIO("in.txt"), inData)
      .input(TextIO("dictionary.txt"), dictionaryData)
      .output(TextIO("out.txt"))(_ should containInAnyOrder (expected))
      .run()
  }
}
