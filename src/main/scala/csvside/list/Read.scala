package csvside
package list

import java.io.{File, Reader}

trait Read {
  self: Types =>

  def read[A](file: File)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(file))

  def read[A](reader: Reader)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(reader))

  def read[A](data: String)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(data))

  def process[A](seq: Seq[Seq[String]])(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] = {
    val cols = seq.head
    seq.tail.map { cells =>
      format((cols zip cells).toMap)
    }
  }
}
