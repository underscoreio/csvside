package csvside

import cats.data.Validated

trait CsvTypes {
  type CsvHead = String
  type CsvCell = String
  type CsvError = String

  type CsvValidated[+A] = Validated[List[CsvError], A]

  type CsvCellFormat[+A] = CsvCell => CsvValidated[A]
}