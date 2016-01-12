package csvside

case class CsvError(line: Int, column: CsvPath, message: String) {
  def prefix(path: String)  = this.copy(column = column prefix path)
  def prefix(path: CsvPath) = this.copy(column = column prefix path)
}

object CsvError {
  def apply(line: Int, message: String): CsvError =
    CsvError(line, CsvPath.empty, message)
}
