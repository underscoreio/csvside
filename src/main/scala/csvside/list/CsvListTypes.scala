package csvside
package list

trait CsvListTypes {
  type CsvListFormat[A] = Map[CsvHead, CsvCell] => CsvValidated[A]
}
