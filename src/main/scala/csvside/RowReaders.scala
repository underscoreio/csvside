package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.traverse._

trait RowReaders extends CellReaders {
  def readConstant[A](value: A): RowReader[A] =
    RowReader[A] { row =>
      valid(value)
    }

  implicit class CsvHeadReaderOps(head: CsvHead) {
    // def prefix(errors: List[CsvError]): List[CsvError] =
    //   errors.map(_.prefix(head + ": "))

    def read[A](implicit reader: CellReader[A]): RowReader[A] =
      RowReader[A] { row =>
        row.get(head) match {
          case Some(cell) => reader(cell)//.bimap(prefix, identity)
          case None => invalid(List(row.error(head, s"$head: Column was empty")))
        }
      }

    def readPair[A](implicit reader: CellReader[A]): RowReader[(CsvHead, A)] =
      read[A].map(value => head -> value)
  }

  implicit class CsvHeadListOps(heads: List[CsvHead]) {
    def readMap[A](implicit reader: CellReader[A]): RowReader[Map[CsvHead, A]] =
      Applicative[RowReader].sequence(heads.map(_.readPair[A])).map(_.toMap)
  }
}
