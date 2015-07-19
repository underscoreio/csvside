package csvside
package grid

trait Types extends core.Types {
  type CsvGrid[A] = Map[(CsvHead, CsvHead), A]
}