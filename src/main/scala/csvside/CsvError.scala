package csvside

case class CsvError(column: CsvPath, message: String) {
  def prefix(path: String)  = this.copy(column = column prefix path)
  def prefix(path: CsvPath) = this.copy(column = column prefix path)
}

object CsvError {
  def apply(message: String): CsvError =
    CsvError(CsvPath.empty, message)
}
