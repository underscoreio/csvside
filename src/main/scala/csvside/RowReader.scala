package csvside

import cats.Applicative
import cats.data.Validated
import cats.instances.list._
import cats.syntax.validated._

trait RowReader[A] {
  def heads: List[CsvPath]

  def read(row: CsvRow): Validated[List[CsvError], A]

  def map[B](func: A => B): RowReader[B] =
    RowReader[B](this.heads)(this.read(_).map(func))

  def validate[B](func: A => Validated[String, B]): RowReader[B] =
    RowReader[B](this.heads)(row => this.read(row).fold(
      error => error.invalid,
      value => func(value).leftMap(msg => heads.map(path => CsvError(path, msg)))
    ))
}

object RowReader {
  def apply[A](heads: List[CsvPath])(func: CsvRow => Validated[List[CsvError], A]): RowReader[A] = {
    val _heads = heads
    new RowReader[A] {
      def heads: List[CsvPath] =
        _heads

      def read(row: CsvRow): Validated[List[CsvError], A] =
        func(row)
    }
  }

  implicit val rowReaderApplicative: Applicative[RowReader] =
    new Applicative[RowReader] {
      private val validatedApplicative: Applicative[Validated[List[CsvError], ?]] =
        Applicative[Validated[List[CsvError], ?]]

      def pure[A](value: A): RowReader[A] =
        apply(Nil)(_ => value.valid)

      override def map[A, B](reader: RowReader[A])(func: A => B): RowReader[B] =
        RowReader[B](reader.heads) { row =>
          reader.read(row).map(func)
        }

      override def product[A, B](reader1: RowReader[A], reader2: RowReader[B]): RowReader[(A, B)] =
        RowReader[(A, B)](reader1.heads ++ reader2.heads) { row =>
          val a = reader1.read(row)
          val b = reader2.read(row)
          validatedApplicative.product(a, b)
        }

      def ap[A, B](reader1: RowReader[A => B])(reader2: RowReader[A]): RowReader[B] =
        RowReader[B](reader2.heads ++ reader1.heads) { row =>
          val a = reader1.read(row)
          val b = reader2.read(row)
          validatedApplicative.ap(a)(b)
        }
    }
}
