package csvside
package list

import cats.Apply
import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.apply._

trait CsvListFormats {
  implicit class CsvListHeadOps(head: CsvHead) {
    def prefix(errors: List[CsvError]): List[CsvError] =
      errors.map(head + ": " + _)

    def as[A](implicit format: CsvCellFormat[A]): CsvListFormat[A] =
      row => row.get(head) match {
        case Some(cell) => format(cell).bimap(prefix, identity)
        case None => invalid(List(s"Column not found: $head"))
      }
  }

  implicit val csvListFormatApply: Apply[CsvListFormat] =
    new Apply[CsvListFormat] {
      def map[A, B](fa: CsvListFormat[A])(f: A => B): CsvListFormat[B] =
        row => fa(row).map(f)

      def ap[A, B](fa: CsvListFormat[A])(f: CsvListFormat[A => B]): CsvListFormat[B] =
        row => {
          val a: CsvValidated[A] = fa(row)
          val b: CsvValidated[A => B] = f(row)
          (applySyntax(b) |@| a) map (_(_))
        }
    }
}
