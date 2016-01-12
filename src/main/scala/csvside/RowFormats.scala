package csvside

import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._

trait RowFormats extends RowReaders with RowWriters {
  implicit class CsvPathFormatOps(head: CsvPath) {
    def csv[A](implicit reader: CellReader[A], writer: CellWriter[A]): RowFormat[A] =
      RowFormat[A](head.read[A], head.write[A])
  }

  implicit class StringFormatOps(head: String) {
    def csv[A](implicit reader: CellReader[A], writer: CellWriter[A]): RowFormat[A] =
      CsvPath(head).csv[A]
  }
}
