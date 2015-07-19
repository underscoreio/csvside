package csvside
package list

import java.io.{File, Reader}

trait Load {
  self: Types =>

  def load[A](file: File)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Load.load(file))

  def read[A](reader: Reader)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Load.read(reader))

  def read[A](data: String)(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] =
    process(core.Load.read(data))

  def process[A](seq: Seq[Seq[String]])(implicit format: ColumnFormat[A]): Seq[CsvValidated[A]] = {
    val cols = seq.head
    seq.tail.map { cells =>
      format((cols zip cells).toMap)
    }
  }
}
