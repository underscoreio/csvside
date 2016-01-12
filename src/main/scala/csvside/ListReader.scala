package csvside

import cats.data.Validated.{valid, invalid}

trait ListReader[A] extends (List[CsvPath] => CsvValidated[RowReader[A]])

object ListReader {
  def apply[A](func: List[String] => CsvValidated[RowReader[A]]): ListReader[A] =
    new ListReader[A] {
      def apply(csv: List[CsvPath]): CsvValidated[RowReader[A]] =
        func(csv.map(_.text))
    }

  implicit def fromRowReader[A](implicit reader: RowReader[A]): ListReader[A] =
    ListReader[A](_ => valid(reader))
}
