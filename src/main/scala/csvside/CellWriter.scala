package csvside

trait CellWriter[-A] extends (A => String) {
  def contramap[B](func: B => A): CellWriter[B] =
    CellWriter[B](value => this(func(value)))
}

object CellWriter {
  def apply[A](func: A => String): CellWriter[A] =
    new CellWriter[A] {
      def apply(value: A): String = func(value)
    }
}
