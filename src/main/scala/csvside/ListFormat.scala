package csvside

import cats.data.Validated.{valid, invalid}

trait ListFormat[A] extends (List[String] => CsvValidated[ColumnFormat[A]])

object ListFormat {
  def apply[A](func: List[String] => CsvValidated[ColumnFormat[A]]): ListFormat[A] =
    new ListFormat[A] {
      def apply(csv: List[String]): CsvValidated[ColumnFormat[A]] =
        func(csv)
    }

  implicit def fromColumnFormat[A](implicit columnFormat: ColumnFormat[A]): ListFormat[A] =
    ListFormat[A](_ => valid(columnFormat))
}
