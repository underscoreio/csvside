package csvside

import com.bizo.mighty.csv.{CSVReader => MightyCsvReader}
import au.com.bytecode.opencsv.{CSVReader => OpenCsvReader}
import java.io.{File, Reader, FileReader, StringReader}
import scala.collection.JavaConversions._
import cats.data.Validated.{valid, invalid}

trait Read extends ReadInternals {
  def fromFile[A](file: File)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(fileIterator(file))

  def fromReader[A](reader: Reader)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(readerIterator(reader))

  def fromString[A](data: String)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(stringIterator(data))

  def fromIterator[A](iterator: Iterator[List[String]])(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] = {
    if(!iterator.hasNext) {
      Iterator.empty
    } else {
      val cols: List[CsvPath] =
        iterator.next.map(CsvPath.apply)

      listReader(cols).fold(
        errors    => Iterator(invalid(errors)),
        rowReader => new Iterator[CsvValidated[A]] {
          var rowNumber = 1 // incremented before use... effectively starts at 2
          def hasNext = iterator.hasNext
          def next = {
            rowNumber = rowNumber + 1
            rowReader
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

trait ReadInternals {
  def fileIterator(file: File): Iterator[List[String]] = {
    val reader = new FileReader(file)
    try readerIterator(reader) finally reader.close()
  }

  def stringIterator(in: String): Iterator[List[String]] =
    readerIterator(new StringReader(in))

  def readerIterator(reader: Reader): Iterator[List[String]] =
    MightyCsvReader(new OpenCsvReader(reader)).map(_.toList)
}
