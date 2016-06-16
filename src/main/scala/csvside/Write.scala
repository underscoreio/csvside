package csvside

import au.com.bytecode.opencsv.{CSVWriter => OpenCsvWriter}
import com.bizo.mighty.csv.{CSVDictWriter => MightyCsvWriter}
import java.io.{File, Writer, FileWriter, StringWriter}
import cats.data.Validated.{valid, invalid}

trait Write {
  def toString[A](items: Seq[A])(implicit rowWriter: RowWriter[A]): String = {
    val out = new StringWriter
    try {
      toWriter(items, out)
      out.toString
    } finally out.close
  }

  def toFile[A](items: Seq[A], file: File)(implicit rowWriter: RowWriter[A]): Unit = {
    val out = new FileWriter(file)
    try toWriter(items, out) finally out.close
  }

  def toWriter[A](items: Seq[A], out: Writer)(implicit rowWriter: RowWriter[A]): Unit = {
    val heads  = rowWriter.heads
    val rows   = items.zipWithIndex map rowWriter.tupledWrite
    val mighty = MightyCsvWriter(new OpenCsvWriter(out), heads map (_.text))
    mighty.writeHeader
    rows.foreach { row =>
      mighty.write(row.values.map { case (head, value) => (head.text) -> value })
    }
  }
}
