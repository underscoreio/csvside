package csvside

trait CellFormat[+A] extends (CsvCell => CsvValidated[A])

object CellFormat {
  def apply[A](func: CsvCell => CsvValidated[A]): CellFormat[A] =
    new CellFormat[A] {
      def apply(csv: CsvCell): CsvValidated[A] =
        func(csv)
    }
}
