package csvside
package list

import cats.data.Validated.{valid, invalid}
import csvside.core._

trait ListFormat[A] extends (List[CsvHead] => CsvValidated[ColumnFormat[A]])

object ListFormat {
  def apply[A](func: List[CsvHead] => CsvValidated[ColumnFormat[A]]): ListFormat[A] =
    new ListFormat[A] {
      def apply(csv: List[CsvHead]): CsvValidated[ColumnFormat[A]] =
        func(csv)
    }

  implicit def columnFormatToListFormat[A](implicit columnFormat: ColumnFormat[A]): ListFormat[A] =
    ListFormat[A](_ => valid(columnFormat))
}
