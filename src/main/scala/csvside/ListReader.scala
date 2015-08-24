package csvside

import cats.data.Validated.{valid, invalid}

trait ListReader[A] extends (List[String] => CsvValidated[ColumnReader[A]])

object ListReader {
  def apply[A](func: List[String] => CsvValidated[ColumnReader[A]]): ListReader[A] =
    new ListReader[A] {
      def apply(csv: List[String]): CsvValidated[ColumnReader[A]] =
        func(csv)
    }

  implicit def fromColumnReader[A](implicit columnReader: ColumnReader[A]): ListReader[A] =
    ListReader[A](_ => valid(columnReader))
}
