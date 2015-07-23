package csvside
package list

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._
import csvside.core._

trait ColumnFormats extends CellFormats {
  implicit class CsvHeadOps(head: CsvHead) {
    def prefix(errors: List[CsvError]): List[CsvError] =
      errors.map(head + ": " + _)

    def as[A](implicit format: CellFormat[A]): ColumnFormat[A] =
      ColumnFormat[A] { row =>
        row.get(head) match {
          case Some(cell) => format(cell).bimap(prefix, identity)
          case None => invalid(List(s"$head: Column was empty"))
        }
      }

    def asPair[A](implicit format: CellFormat[A]): ColumnFormat[(String, A)] =
      as[A].map(value => head -> value)
  }

  implicit class CsvHeadListOps(heads: List[CsvHead]) {
    def asMap[A](implicit format: CellFormat[A]): ColumnFormat[Map[CsvHead, A]] =
      Applicative[ColumnFormat].sequence(heads.map(_.asPair[A])).map(_.toMap)
  }
}
