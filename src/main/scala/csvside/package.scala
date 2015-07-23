import cats.data.Validated

package object csvside extends ColumnFormats with Read {
  type CsvHead = String
  type CsvCell = String
  type CsvError = String

  type CsvValidated[+A] = Validated[List[CsvError], A]
}