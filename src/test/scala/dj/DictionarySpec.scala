package dj

import com.spotify.scio.io.TextIO
import com.spotify.scio.testing._

class DictionarySpec extends PipelineSpec {

  val inData = Seq("hello world\n", "what's up World?\n")
  val expected = Seq("hello 99162322", "world 113318802", "what's -789049616", "up 3739")

  "Dictionary" should "create dictionary from the words of the input data" in {
    JobTest[dj.Dictionary.type]
      .args("--input=in.txt", "--output=out.txt")
      .input(TextIO("in.txt"), inData)
      .output(TextIO("out.txt"))(_ should containInAnyOrder (expected))
      .run()
  }
}
