package csvside

import scala.language.higherKinds
import cats.Monoidal
import cats.functor.Contravariant

trait RowWriter[-A] {
  def heads: List[CsvPath]

  def write(value: A, row: Int): CsvRow

  def tupledWrite(pair: (A, Int)): CsvRow =
    write(pair._1, pair._2)

  def contramap[B](func: B => A): RowWriter[B] =
    RowWriter[B](heads)((value, row) => this.write(func(value), row))

  def ~[B <: A](that: RowWriter[B]): RowWriter[B] =
    RowWriter[B](this.heads ++ that.heads) { (value, row) =>
      val a = this.write(value, row)
      val b = that.write(value, row)
      CsvRow(row, a.values ++ b.values)
    }
}

object RowWriter {
  def apply[A](heads: List[CsvPath])(func: (A, Int) => CsvRow): RowWriter[A] = {
    val _heads = heads
    new RowWriter[A] {
      val heads = _heads
      def write(value: A, row: Int) = func(value, row)
    }
  }

  implicit val rowWriterMonoidal: Monoidal[RowWriter] =
    new Monoidal[RowWriter] {
      def product[A, B](writer1: RowWriter[A], writer2: RowWriter[B]): RowWriter[(A, B)] =
        RowWriter[(A, B)](writer1.heads ++ writer2.heads) { (value, row) =>
          val row1 = writer1.write(value._1, row)
          val row2 = writer2.write(value._2, row)
          CsvRow(row, row1.values ++ row2.values)
        }
    }

  implicit val rowWriterFunctor: Contravariant[RowWriter] =
    new Contravariant[RowWriter] {
      def contramap[A, B](writer: RowWriter[A])(func: B => A): RowWriter[B] =
        RowWriter[B](writer.heads) { (value, row) =>
          writer.write(func(value), row)
        }
    }
}
