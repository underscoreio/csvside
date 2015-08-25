package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._

trait RowWriters extends CellWriters {
  def writeConstant[A](head: CsvHead)(value: A)(implicit writer: CellWriter[A]) =
    RowWriter[A](List(head)) { (value, row) =>
      CsvRow(row, Map(head -> writer(value)))
    }

  implicit class CsvHeadWriterOps(head: CsvHead) {
    def write[A](implicit writer: CellWriter[A]): RowWriter[A] =
      RowWriter[A](List(head)) { (value, row) =>
        CsvRow(row, Map(head -> writer(value)))
      }
  }
}
