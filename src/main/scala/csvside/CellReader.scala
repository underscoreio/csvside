package csvside

trait CellReader[+A] extends (CsvCell => CsvValidated[A]) {
  def map[B](func: A => B): CellReader[B] =
    CellReader[B](cell => this(cell).map(func))
}

object CellReader {
  def apply[A](func: CsvCell => CsvValidated[A]): CellReader[A] =
    new CellReader[A] {
      def apply(csv: CsvCell): CsvValidated[A] =
        func(csv)
    }
}
