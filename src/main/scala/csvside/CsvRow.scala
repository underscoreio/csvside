package csvside

case class CsvRow(number: Int, values: Map[CsvHead, String]) {
  def get(head: CsvHead): Option[CsvCell] =
    values.get(head).map(CsvCell(number, head, _))

  def error(head: CsvHead, msg: String) =
    CsvError(number, head, msg)
}
