package csvside
package core

import java.io.{File, Reader, FileReader, StringReader}

import scala.collection.JavaConversions._

private[csvside] object Read {
  def read(file: File): Seq[List[String]] = {
    val reader = new FileReader(file)
    try read(reader) finally reader.close()
  }

  def read(in: String): Seq[List[String]] =
    read(new StringReader(in))

  def read(reader: Reader): Seq[List[String]] = {
    val csv = new com.opencsv.CSVReader(reader)

    def stream: Stream[List[String]] = {
      val next = csv.readNext()
      if (next == null) Stream.Empty else next.toList #:: stream
    }

    stream
  }
}
