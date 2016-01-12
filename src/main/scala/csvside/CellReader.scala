package csvside

import cats.data.Validated

trait CellReader[+A] extends (String => Validated[String, A]) {
  def map[B](func: A => B): CellReader[B] =
    CellReader[B](cell => this(cell).map(func))
}

object CellReader {
  def apply[A](func: String => Validated[String, A]): CellReader[A] =
    new CellReader[A] {
      def apply(csv: String): Validated[String, A] =
        func(csv)
    }
}
