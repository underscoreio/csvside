import cats.data.Validated

package object csvside extends RowReaders with RowWriters with Read with Write {
  type RowNumber = Int

  type CsvHead = String

  type CsvValidated[+A] = Validated[List[CsvError], A]
}
