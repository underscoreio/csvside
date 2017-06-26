package object csvside extends RowFormats {
  type RowNumber = Int

  object Csv extends Read with Write
}
