package csvside

import cats.data.Validated
// import cats.std.all._
import cats.syntax.traverse._
import cats.syntax.validated._
import scala.collection.mutable

trait RowReaders extends CellReaders {
  def readLineNumber: RowReader[Int] =
    RowReader[Int](CsvPath.emptyList) { row => row.number.valid }

  def readConstant[A](value: A): RowReader[A] =
    RowReader[A](CsvPath.emptyList) { row => value.valid }

  implicit class CsvPathReaderOps(head: CsvPath) {
    def read[A](implicit reader: CellReader[A]): RowReader[A] =
      RowReader[A](List(head)) { row =>
        row.get(head) match {
          case Some(cell) => reader(cell.value) leftMap (msg => List(CsvError(row.number, head, msg)))
          case None       => List(CsvError(row.number, head, s"Column was empty")).invalid
        }
      }

    def readPair[A](implicit reader: CellReader[A]): RowReader[(String, A)] =
      read[A].map(value => head.text -> value)
  }

  implicit class StringReaderOps(head: String) {
    def read[A](implicit reader: CellReader[A]): RowReader[A] =
      CsvPath(head).read[A]

    def readPair[A](implicit reader: CellReader[A]): RowReader[(String, A)] =
      CsvPath(head).readPair[A]
  }

  implicit class CsvPathListOps(heads: List[CsvPath]) {
    def readMap[A](implicit reader: CellReader[A]): RowReader[Map[String, A]] =
      // NOTE: The previous implementation of readMap
      // was very pretty and used applicatives and stuff,
      // but caused stack overflows on very large column sets.
      // This implementation is uglier but more efficient.
      RowReader[Map[String, A]](heads) { row =>
        val invalid = new mutable.ArrayBuffer[CsvError]()
        val valid   = new mutable.HashMap[String, A]()
        heads.foreach { head =>
          row.get(head) match {
            case Some(cell) =>
              reader(cell.value) match {
                case Validated.Valid(value) => valid.put(head.text, value)
                case Validated.Invalid(msg) => invalid += CsvError(row.number, head, msg)
              }
            case None =>
              invalid += CsvError(row.number, head, s"Column was empty")
          }
        }
        if(invalid.isEmpty) {
          Validated.valid(valid.toMap)
        } else {
          Validated.invalid(invalid.toList)
        }
      }
  }

  implicit class StringListOps(heads: List[String]) {
    def readMap[A](implicit reader: CellReader[A]): RowReader[Map[String, A]] =
      heads.map(CsvPath(_)).readMap[A]
  }
}
