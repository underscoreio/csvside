package csvside

import au.com.bytecode.opencsv.{CSVWriter => OpenCsvWriter}
import com.bizo.mighty.csv.{CSVDictWriter => MightyCsvWriter}
import java.io.{File, Writer, FileWriter, StringWriter}
import scala.collection.JavaConversions._
import cats.data.Validated.{valid, invalid}

trait Write extends WriteRaw {
  def csvString[A](items: Seq[A])(implicit rowWriter: RowWriter[A]): String =
    writeRawString(rowWriter.heads, items.zipWithIndex map rowWriter.tupledWrite)

  def writeCsvFile[A](items: Seq[A], file: File)(implicit rowWriter: RowWriter[A]): Unit =
    writeRawFile(rowWriter.heads, items.zipWithIndex map rowWriter.tupledWrite, file)

  def writeCsv[A](items: Seq[A], writer: Writer)(implicit rowWriter: RowWriter[A]): Unit =
    writeRaw(rowWriter.heads, items.zipWithIndex map rowWriter.tupledWrite, writer)
}

trait WriteRaw {
  private[csvside] def writeRawString(heads: List[CsvHead], rows: Seq[CsvRow]): String = {
    val writer = new StringWriter()
    try {
      writeRaw(heads, rows, writer)
      writer.toString()
    } finally writer.close()
  }

  private[csvside] def writeRawFile(heads: List[CsvHead], rows: Seq[CsvRow], file: File): Unit = {
    val writer = new FileWriter(file)
    try writeRaw(heads, rows, writer) finally writer.close()
  }

  private[csvside] def writeRaw(heads: List[CsvHead], rows: Seq[CsvRow], writer: Writer): Unit = {
    val output = MightyCsvWriter(new OpenCsvWriter(writer), heads)
    output.writeHeader()
    rows.foreach(row => output.write(row.values))
  }
}
