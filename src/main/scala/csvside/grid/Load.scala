package csvside
package grid

import cats.data.Validated
import cats.std.all._
import cats.syntax.traverse._
import java.io.{File, Reader}

trait Load {
  self: Types =>

  def read[A](data: String)(implicit format: CellFormat[A]): CsvValidated[CsvGrid[A]] =
    process(core.Load.read(data))

  def read[A](reader: Reader)(implicit format: CellFormat[A]): CsvValidated[CsvGrid[A]] =
    process(core.Load.read(reader))

  def load[A](file: File)(implicit format: CellFormat[A]): CsvValidated[CsvGrid[A]] =
    process(core.Load.load(file))

  def process[A](csv: Seq[Seq[String]])(implicit format: CellFormat[A]): CsvValidated[CsvGrid[A]] = {
    val valPairs: Seq[CsvValidated[((CsvHead, CsvHead), A)]] = {
      val cols = csv.head.tail

      for {
        row +: rawCells <- csv.tail
        (col, rawCell)  <- cols zip rawCells
      } yield {
        val valCell = format(rawCell)
        valCell.bimap(
          errors => errors.map(error => s"${col}, ${row}: ${error}"),
          cell => (col, row) -> cell
        )
      }
    }

    valPairs.toList.sequence.map(_.toMap)
  }
}
