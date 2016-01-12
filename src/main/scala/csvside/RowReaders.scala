package csvside

import cats.std.all._
import cats.syntax.traverse._
import cats.syntax.validated._

trait RowReaders extends CellReaders {
  def readConstant[A](value: A): RowReader[A] =
    RowReader[A](CsvPath.emptyList) { row => value.valid }

  implicit class CsvPathReaderOps(head: CsvPath) {
    def read[A](implicit reader: CellReader[A]): RowReader[A] =
      RowReader[A](List(head)) { row =>
        row.get(head) match {
          case Some(cell) => reader(cell.value) leftMap (msg => List(CsvError(row.number, head, msg)))
          case None       => List(CsvError(row.number, head, s"Column was empty")).invalid
        }
      }

    def readPair[A](implicit reader: CellReader[A]): RowReader[(String, A)] =
      read[A].map(value => head.text -> value)
  }

  implicit class StringReaderOps(head: String) {
    def read[A](implicit reader: CellReader[A]): RowReader[A] =
      CsvPath(head).read[A]

    def readPair[A](implicit reader: CellReader[A]): RowReader[(String, A)] =
      CsvPath(head).readPair[A]
  }

  implicit class CsvPathListOps(heads: List[CsvPath]) {
    def readMap[A](implicit reader: CellReader[A]): RowReader[Map[String, A]] =
      heads.map(_.readPair[A]).sequence.map(_.toMap)
  }

  implicit class StringListOps(heads: List[String]) {
    def readMap[A](implicit reader: CellReader[A]): RowReader[Map[String, A]] =
      heads.map(CsvPath(_)).readMap[A]
  }
}
