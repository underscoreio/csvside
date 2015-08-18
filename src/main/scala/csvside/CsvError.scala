package csvside

case class CsvError(line: Int, column: CsvHead, message: String) {
  def prefix(prefix: String) = this.copy(message = prefix + message)
}