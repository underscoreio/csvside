package csvside

import java.io.{File, Reader, FileReader, StringReader}
import scala.collection.JavaConversions._
import cats.data.Validated.{valid, invalid}

trait Read extends ReadRaw {
  def read[A](file: File)(implicit format: ListReader[A]): Seq[CsvValidated[A]] =
    process(readRaw(file))

  def read[A](reader: Reader)(implicit format: ListReader[A]): Seq[CsvValidated[A]] =
    process(readRaw(reader))

  def read[A](data: String)(implicit format: ListReader[A]): Seq[CsvValidated[A]] =
    process(readRaw(data))

  def process[A](seq: Seq[List[String]])(implicit listReader: ListReader[A]): Seq[CsvValidated[A]] = {
    val cols = seq.head

    listReader(cols).fold(
      errors => Seq(invalid(errors)),
      format => seq.tail.zipWithIndex map {
        case (cells, index) =>
          format(CsvRow(index + 2, (cols zip cells).toMap)).fold(
            errors => invalid(errors),
            result => valid(result)
          )
      }
    )
  }
}

trait ReadRaw {
  private[csvside] def readRaw(file: File): Seq[List[String]] = {
    val reader = new FileReader(file)
    try readRaw(reader) finally reader.close()
  }

  private[csvside] def readRaw(in: String): Seq[List[String]] =
    readRaw(new StringReader(in))

  private[csvside] def readRaw(reader: Reader): Seq[List[String]] = {
    val csv = new com.opencsv.CSVReader(reader)

    def stream: Stream[List[String]] = {
      val next = csv.readNext()
      if (next == null) Stream.Empty else next.toList #:: stream
    }

    stream
  }
}