package csvside

import cats.data.Validated
import cats.syntax.validated._

trait ListReader[A] extends (List[CsvPath] => Validated[List[CsvError], RowReader[A]])

object ListReader {
  def apply[A](func: List[String] => Validated[List[CsvError], RowReader[A]]): ListReader[A] =
    new ListReader[A] {
      def apply(csv: List[CsvPath]): Validated[List[CsvError], RowReader[A]] =
        func(csv.map(_.text))
    }

  implicit def fromRowReader[A](implicit reader: RowReader[A]): ListReader[A] =
    ListReader[A](_ => reader.valid)
}
