package csvside

import cats.data.Validated

package object core {
  type CsvHead = String
  type CsvCell = String
  type CsvError = String

  type CsvValidated[+A] = Validated[List[CsvError], A]
}