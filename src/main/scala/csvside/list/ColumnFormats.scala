package csvside
package list

import cats.Apply
import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.apply._
import core.CellFormats

trait ColumnFormats extends CellFormats {
  self: Types =>

  implicit class CsvListHeadOps(head: CsvHead) {
    def prefix(errors: List[CsvError]): List[CsvError] =
      errors.map(head + ": " + _)

    def as[A](implicit format: CellFormat[A]): ColumnFormat[A] =
      row => row.get(head) match {
        case Some(cell) => format(cell).bimap(prefix, identity)
        case None => invalid(List(s"$head: Column was empty"))
      }
  }

  implicit val csvListFormatApply: Apply[ColumnFormat] =
    new Apply[ColumnFormat] {
      def map[A, B](fa: ColumnFormat[A])(f: A => B): ColumnFormat[B] =
        row => fa(row).map(f)

      def ap[A, B](fa: ColumnFormat[A])(f: ColumnFormat[A => B]): ColumnFormat[B] =
        row => {
          val a: CsvValidated[A] = fa(row)
          val b: CsvValidated[A => B] = f(row)
          (applySyntax(b) |@| a) map (_(_))
        }
    }
}
