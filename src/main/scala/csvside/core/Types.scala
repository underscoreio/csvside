package csvside
package core

import cats.data.Validated

trait Types {
  type CsvHead = String
  type CsvCell = String
  type CsvError = String

  type CsvValidated[+A] = Validated[List[CsvError], A]

  type CellFormat[+A] = CsvCell => CsvValidated[A]
}