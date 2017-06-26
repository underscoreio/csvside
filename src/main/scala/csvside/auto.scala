package csvside

import cats.data.Validated
import cats.instances.list._
import cats.syntax.cartesian._
import cats.syntax.validated._
import shapeless._
import shapeless.labelled._

object auto {
  implicit val hnilRowReader: RowReader[HNil] =
    RowReader(Nil)(_ => HNil.valid)

  implicit def hconsRowReader[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hReader: Lazy[CellReader[H]],
    tReader: RowReader[T]
  ): RowReader[FieldType[K, H] :: T] = {
    val path = CsvPath(List(witness.value.name))
    val hReader1 = path.read(hReader.value.map(h => field[K](h)))
    (hReader1 |@| tReader).map(_ :: _)
  }

  implicit def genericRowReader[A, R](
    implicit
    generic: LabelledGeneric.Aux[A, R],
    reader: RowReader[R]
  ): RowReader[A] =
    reader.map(generic.from)

  implicit val hnilRowWriter: RowWriter[HNil] =
    RowWriter(Nil) { (value, num) =>
      CsvRow(num, Map.empty)
    }

  implicit def hconsRowWriter[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hWriter: Lazy[CellWriter[H]],
    tWriter: RowWriter[T]
  ): RowWriter[FieldType[K, H] :: T] = {
    val path = CsvPath(List(witness.value.name))
    val hWriter1 = path.write(hWriter.value)
    RowWriter[H :: T](hWriter1.heads ++ tWriter.heads) { (value, num) =>
      val row1 = hWriter1.write(value.head, num)
      val row2 = tWriter.write(value.tail, num)
      CsvRow(num, row1.values ++ row2.values)
    }
  }

  implicit def genericRowWriter[A, R](
    implicit
    generic: LabelledGeneric.Aux[A, R],
    writer: RowWriter[R]
  ): RowWriter[A] =
    writer.contramap(generic.to)
}
