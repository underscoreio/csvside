package csvside

import com.bizo.mighty.csv.{CSVReader => MightyCsvReader}
import au.com.bytecode.opencsv.{CSVReader => OpenCsvReader}
import java.io.{File, Reader, FileReader, StringReader}
import scala.collection.JavaConversions._
import cats.data.Validated.{valid, invalid}

trait Read extends ReadRaw {
  def read[A: ListReader](file: File): Stream[CsvValidated[A]] =
    process(readRaw(file))

  def read[A: ListReader](reader: Reader): Stream[CsvValidated[A]] =
    process(readRaw(reader))

  def read[A: ListReader](data: String): Stream[CsvValidated[A]] =
    process(readRaw(data))

  def process[A](seq: Stream[List[String]])(implicit reader: ListReader[A]): Stream[CsvValidated[A]] = {
    val cols = seq.head

    reader(cols).fold(
      errors => Stream(invalid(errors)),
      reader => seq.tail.zipWithIndex map {
        case (cells, index) =>
          reader.read(CsvRow(index + 2, (cols zip cells).toMap)).fold(
            errors => invalid(errors),
            result => valid(result)
          )
      }
    )
  }
}

trait ReadRaw {
  private[csvside] def readRaw(file: File): Stream[List[String]] = {
    val reader = new FileReader(file)
    try readRaw(reader) finally reader.close()
  }

  private[csvside] def readRaw(in: String): Stream[List[String]] =
    readRaw(new StringReader(in))

  private[csvside] def readRaw(reader: Reader): Stream[List[String]] =
    MightyCsvReader(new OpenCsvReader(reader)).map(_.toList).toStream
}
