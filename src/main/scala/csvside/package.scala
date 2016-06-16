import cats.data.Validated

package object csvside extends RowFormats {
  type RowNumber = Int

  type CsvValidated[+A] = Validated[List[CsvError], A]

  object Csv extends Read with Write
}
