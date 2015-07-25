package csvside

trait CellFormat[+A] extends (CsvCell => CsvValidated[A]) {
  def map[B](func: A => B): CellFormat[B] =
    CellFormat[B](this(_).map(func))
}

object CellFormat {
  def apply[A](func: CsvCell => CsvValidated[A]): CellFormat[A] =
    new CellFormat[A] {
      def apply(csv: CsvCell): CsvValidated[A] =
        func(csv)
    }
}
