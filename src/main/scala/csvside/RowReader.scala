package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.list._
import cats.syntax.monoidal._

trait RowReader[+A] {
  def read(row: CsvRow): CsvValidated[A]

  def map[B](func: A => B): RowReader[B] =
    RowReader[B](this.read(_).map(func))
}

object RowReader {
  def apply[A](func: CsvRow => CsvValidated[A]): RowReader[A] =
    new RowReader[A] {
      def read(row: CsvRow): CsvValidated[A] =
        func(row)
    }

  implicit val rowReaderApplicative: Applicative[RowReader] =
    new Applicative[RowReader] {
      def pure[A](value: A): RowReader[A] =
        apply(_ => valid(value))

      def map[A, B](reader: RowReader[A])(func: A => B): RowReader[B] =
        RowReader[B] { row =>
          reader.read(row).map(func)
        }

      def product[A, B](reader1: RowReader[A], reader2: RowReader[B]): RowReader[(A, B)] =
        RowReader[(A, B)] { row =>
          val a: CsvValidated[A] = reader1.read(row)
          val b: CsvValidated[B] = reader2.read(row)
          (a |@| b).tupled
        }

      def ap[A, B](reader1: RowReader[A])(reader2: RowReader[A => B]): RowReader[B] =
        RowReader[B] { row =>
          val a: CsvValidated[A] = reader1.read(row)
          val b: CsvValidated[A => B] = reader2.read(row)
          (b |@| a) map (_(_))
        }
    }
}
