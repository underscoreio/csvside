package csvside

trait RowWriter[-A] extends ((A, Int) => CsvRow) {
  def heads: List[CsvHead]

  def contramap[B](func: B => A): RowWriter[B] =
    RowWriter[B](heads)((value, row) => this(func(value), row))

  def ~[B <: A](that: RowWriter[B]): RowWriter[B] =
    RowWriter[B](this.heads ++ that.heads) { (value, row) =>
      val a = this(value, row)
      val b = that(value, row)
      CsvRow(row, a.values ++ b.values)
    }
}

object RowWriter {
  def apply[A](heads: List[CsvHead])(func: (A, Int) => CsvRow): RowWriter[A] = {
    val _heads = heads
    new RowWriter[A] {
      val heads = _heads
      def apply(value: A, row: Int) = func(value, row)
    }
  }
}
