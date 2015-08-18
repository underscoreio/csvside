package csvside

case class CsvCell(row: Int, column: CsvHead, value: String) {
  def error(message: String) = CsvError(row, column, message)
}
