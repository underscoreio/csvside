package csvside

case class CsvRow(number: Int, values: Map[CsvPath, String]) {
  def get(head: CsvPath): Option[CsvCell] =
    values.get(head).map(CsvCell(number, _))
}
