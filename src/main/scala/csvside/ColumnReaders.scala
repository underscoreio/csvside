package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._

trait ColumnReaders extends CellReaders {
  def constant[A](value: A): ColumnReader[A] =
    ColumnReader[A] { row =>
      valid(value)
    }

  implicit class CsvHeadOps(head: CsvHead) {
    // def prefix(errors: List[CsvError]): List[CsvError] =
    //   errors.map(_.prefix(head + ": "))

    def as[A](implicit format: CellReader[A]): ColumnReader[A] =
      ColumnReader[A] { row =>
        row.get(head) match {
          case Some(cell) => format(cell)//.bimap(prefix, identity)
          case None => invalid(List(row.error(head, s"$head: Column was empty")))
        }
      }

    def asPair[A](implicit format: CellReader[A]): ColumnReader[(CsvHead, A)] =
      as[A].map(value => head -> value)
  }

  implicit class CsvHeadListOps(heads: List[CsvHead]) {
    def asMap[A](implicit format: CellReader[A]): ColumnReader[Map[CsvHead, A]] =
      Applicative[ColumnReader].sequence(heads.map(_.asPair[A])).map(_.toMap)
  }
}
