import cats.data.Validated

package object csvside extends RowFormats with Read with Write {
  type RowNumber = Int

  type CsvValidated[+A] = Validated[List[CsvError], A]
}
