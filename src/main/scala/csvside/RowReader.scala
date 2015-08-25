package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.apply._

trait RowReader[A] extends (CsvRow => CsvValidated[A]) {
  def map[B](func: A => B): RowReader[B] =
    RowReader[B](this(_).map(func))
}

object RowReader {
  def apply[A](func: CsvRow => CsvValidated[A]): RowReader[A] =
    new RowReader[A] {
      def apply(row: CsvRow): CsvValidated[A] =
        func(row)
    }

  implicit val rowReaderApplicative: Applicative[RowReader] =
    new Applicative[RowReader] {
      def pure[A](x: A): RowReader[A] =
        apply(_ => valid(x))

      def ap[A, B](fa: RowReader[A])(f: RowReader[A => B]): RowReader[B] =
        RowReader[B] { row =>
          val a: CsvValidated[A] = fa(row)
          val b: CsvValidated[A => B] = f(row)
          (applySyntax(b) |@| a) map (_(_))
        }
    }
}
