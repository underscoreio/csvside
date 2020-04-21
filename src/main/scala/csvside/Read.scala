package csvside

import au.com.bytecode.opencsv.{CSVReader => OpenCSVReader}
import java.io.{File, Reader, FileReader, StringReader}

trait Read extends ReadInternals {
  def fromFile[A](file: File)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(fileIterator(file))

  def fromReader[A](reader: Reader)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(readerIterator(reader))

  def fromString[A](data: String)(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] =
    fromIterator(stringIterator(data))

  def fromIterator[A](sourceIterator: Iterator[List[String]])(implicit listReader: ListReader[A]): Iterator[CsvValidated[A]] = {
    if(!sourceIterator.hasNext) {
      Iterator.empty
    } else {
      val cells: List[String] = sourceIterator.next

      val cols: List[CsvPath] =
        cells.map(CsvPath.apply)

      listReader(cols).fold(
        errors => Iterator(CsvFailure(1, cells.mkString(","), errors)),
        rowReader => new Iterator[CsvValidated[A]] {
          var rowNumber = 1 // incremented before use... effectively starts at 2
          def hasNext = sourceIterator.hasNext

          def next: CsvValidated[A] = {
            rowNumber = rowNumber + 1
            val cells = sourceIterator.next
            rowReader
              .read(CsvRow(rowNumber, (cols zip cells).toMap))
              .fold(
                errors => CsvFailure(rowNumber, cells.mkString(","), errors),
                result => CsvSuccess(rowNumber, cells.mkString(","), result)
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
    new Mighty.CSVReader(new OpenCSVReader(reader)).map(_.toList)

}
