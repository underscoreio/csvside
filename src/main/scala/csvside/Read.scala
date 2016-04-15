package csvside

import com.bizo.mighty.csv.{CSVReader => MightyCsvReader}
import au.com.bytecode.opencsv.{CSVReader => OpenCsvReader}
import java.io.{File, Reader, FileReader, StringReader}
import scala.collection.JavaConversions._
import cats.data.Validated.{valid, invalid}

trait Read extends ReadRaw {
  def read[A: ListReader](file: File): Iterator[CsvValidated[A]] =
    process(readRaw(file))

  def read[A: ListReader](reader: Reader): Iterator[CsvValidated[A]] =
    process(readRaw(reader))

  def read[A: ListReader](data: String): Iterator[CsvValidated[A]] =
    process(readRaw(data))

  def process[A](iterator: Iterator[List[String]])(implicit reader: ListReader[A]): Iterator[CsvValidated[A]] = {
    if(!iterator.hasNext) {
      Iterator.empty
    } else {
      val cols: List[CsvPath] =
        iterator.next.map(CsvPath.apply)

      reader(cols).fold(
        errors => Iterator(invalid(errors)),
        reader => new Iterator[CsvValidated[A]] {
          var rowNumber = 1 // incremented before use... effectively starts at 2
          def hasNext = iterator.hasNext
          def next = {
            rowNumber = rowNumber + 1
            reader
              .read(CsvRow(rowNumber, (cols zip iterator.next).toMap))
              .fold(
                errors => invalid(errors),
                result => valid(result)
              )
          }
        }
      )
    }
  }
}

trait ReadRaw {
  private[csvside] def readRaw(file: File): Iterator[List[String]] = {
    val reader = new FileReader(file)
    try readRaw(reader) finally reader.close()
  }

  private[csvside] def readRaw(in: String): Iterator[List[String]] =
    readRaw(new StringReader(in))

  private[csvside] def readRaw(reader: Reader): Iterator[List[String]] =
    MightyCsvReader(new OpenCsvReader(reader)).map(_.toList)
}
