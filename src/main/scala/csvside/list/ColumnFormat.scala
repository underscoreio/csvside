package csvside
package list

import cats.Applicative
import cats.data.Validated.{valid, invalid}
import cats.std.all._
import cats.syntax.apply._
import csvside.core._

trait ColumnFormat[A] extends (Map[CsvHead, CsvCell] => CsvValidated[A]) {
  def map[B](func: A => B): ColumnFormat[B] =
    ColumnFormat[B](this(_).map(func))
}

object ColumnFormat {
  def apply[A](func: Map[CsvHead, CsvCell] => CsvValidated[A]): ColumnFormat[A] =
    new ColumnFormat[A] {
      def apply(row: Map[CsvHead, CsvCell]): CsvValidated[A] =
        func(row)
    }

  implicit val csvListFormatApplicative: Applicative[ColumnFormat] =
    new Applicative[ColumnFormat] {
      def pure[A](x: A): ColumnFormat[A] =
        apply(_ => valid(x))

      // def map[A, B](fa: ColumnFormat[A])(f: A => B): ColumnFormat[B] =
      //   ColumnFormat[B] { row =>
      //     fa(row).map(f)
      //   }

      def ap[A, B](fa: ColumnFormat[A])(f: ColumnFormat[A => B]): ColumnFormat[B] =
        ColumnFormat[B] { row =>
          val a: CsvValidated[A] = fa(row)
          val b: CsvValidated[A => B] = f(row)
          (applySyntax(b) |@| a) map (_(_))
        }
    }
}
