package csvside

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.apply._

trait ColumnReader[A] extends (CsvRow => CsvValidated[A]) {
  def map[B](func: A => B): ColumnReader[B] =
    ColumnReader[B](this(_).map(func))
}

object ColumnReader {
  def apply[A](func: CsvRow => CsvValidated[A]): ColumnReader[A] =
    new ColumnReader[A] {
      def apply(row: CsvRow): CsvValidated[A] =
        func(row)
    }

  implicit val csvListReaderApplicative: Applicative[ColumnReader] =
    new Applicative[ColumnReader] {
      def pure[A](x: A): ColumnReader[A] =
        apply(_ => valid(x))

      // def map[A, B](fa: ColumnReader[A])(f: A => B): ColumnReader[B] =
      //   ColumnReader[B] { row =>
      //     fa(row).map(f)
      //   }

      def ap[A, B](fa: ColumnReader[A])(f: ColumnReader[A => B]): ColumnReader[B] =
        ColumnReader[B] { row =>
          val a: CsvValidated[A] = fa(row)
          val b: CsvValidated[A => B] = f(row)
          (applySyntax(b) |@| a) map (_(_))
        }
    }
}
