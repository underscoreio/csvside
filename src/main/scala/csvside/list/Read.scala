package csvside
package list

import cats.data.Validated.{valid, invalid}
import java.io.{File, Reader}
import csvside.core._

trait Read {
  def read[A](file: File)(implicit format: ListFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(file))

  def read[A](reader: Reader)(implicit format: ListFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(reader))

  def read[A](data: String)(implicit format: ListFormat[A]): Seq[CsvValidated[A]] =
    process(core.Read.read(data))

  def process[A](seq: Seq[List[String]])(implicit listFormat: ListFormat[A]): Seq[CsvValidated[A]] = {
    val cols = seq.head

    listFormat(cols).map(x => { println("!!!" + x); x }).fold(
      errors => { println(errors); Seq(invalid(errors)) },
      format => { println(format); seq.tail.map(cells => format((cols zip cells).toMap)) }
    )
  }
}
