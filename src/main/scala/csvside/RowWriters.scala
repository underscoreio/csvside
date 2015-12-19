package csvside

import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._

trait RowWriters extends CellWriters {
  implicit class CsvHeadWriterOps(head: CsvHead) {
    def writeConstant[A](value: A)(implicit writer: CellWriter[A]) =
      RowWriter[A](List(head)) { (value, row) =>
        CsvRow(row, Map(head -> writer(value)))
      }

    def write[A](implicit writer: CellWriter[A]): RowWriter[A] =
      RowWriter[A](List(head)) { (value, row) =>
        CsvRow(row, Map(head -> writer(value)))
      }
  }

  def unlift[A, B](func: A => Option[B]): A => B =
    (value: A) => func(value).get
}
