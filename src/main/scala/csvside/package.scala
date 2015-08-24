import cats.data.Validated

package object csvside extends ColumnReaders with Read {
  type RowNumber = Int

  type CsvHead = String

  type CsvValidated[+A] = Validated[List[CsvError], A]
}
