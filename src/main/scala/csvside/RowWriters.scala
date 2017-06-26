package csvside

import cats.data.Validated.{valid, invalid}
import cats.instances.all._
import cats.syntax.traverse._

trait RowWriters extends CellWriters {
  implicit class CsvPathWriterOps(head: CsvPath) {
    def writeConstant[A](value: A)(implicit writer: CellWriter[A]) =
      RowWriter[A](List(head)) { (value, row) =>
        CsvRow(row, Map(head -> writer(value)))
      }

    def write[A](implicit writer: CellWriter[A]): RowWriter[A] =
      RowWriter[A](List(head)) { (value, row) =>
        CsvRow(row, Map(head -> writer(value)))
      }
  }

  implicit class StringWriterOps(head: String) {
    def writeConstant[A](value: A)(implicit writer: CellWriter[A]) =
      CsvPath(head).writeConstant[A](value)

    def write[A](implicit writer: CellWriter[A]): RowWriter[A] =
      CsvPath(head).write[A]
  }

  def unlift[A, B](func: A => Option[B]): A => B =
    (value: A) => func(value).get
}
