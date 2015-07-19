package csvside
package list

trait Types extends core.Types {
  type ColumnFormat[A] = Map[CsvHead, CsvCell] => CsvValidated[A]
}
