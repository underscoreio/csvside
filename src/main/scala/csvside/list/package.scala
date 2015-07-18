package csvside

import java.io.File

package object list extends CsvListTypes with CsvListFormats {
  def read[A](data: String)(implicit format: CsvListFormat[A]): Seq[CsvValidated[A]] =
    seqToMap(csvside.core.read(data)).map(format)

  def load[A](file: File)(implicit format: CsvListFormat[A]): Seq[CsvValidated[A]] =
    seqToMap(csvside.core.load(file)).map(format)

  def seqToMap(seq: Seq[Seq[String]]): Seq[Map[CsvHead, CsvCell]] = {
    val cols = seq.head
    seq.tail.map(cells => cols.zip(cells).toMap)
  }
}
