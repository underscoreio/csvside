package csvside
package core

import java.io.{File, Reader, FileReader, StringReader}

import scala.collection.JavaConversions._

private[csvside] object Load {
  def load(file: File): Seq[Seq[String]] = {
    val reader = new FileReader(file)
    try read(reader) finally reader.close()
  }

  def read(in: String): Seq[Seq[String]] =
    read(new StringReader(in))

  def read(reader: Reader): Seq[Seq[String]] = {
    val csv = new com.opencsv.CSVReader(reader)

    def stream: Stream[Array[String]] = {
      val next = csv.readNext()
      if (next == null) Stream.Empty else next #:: stream
    }

    stream.map(_.toSeq)
  }
}
