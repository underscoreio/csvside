package csvside

import au.com.bytecode.opencsv.{ CSVReader => OpenCSVReader }
import au.com.bytecode.opencsv.{ CSVWriter => OpenCSVWriter }

/*
 * This code from Apache 2.0 Licensed mighty-csv
 *
 * https://github.com/t-pleasure/mighty-csv/ at version 2dcee78
*/
object Mighty {

  type Row = Array[String]

  class CSVReader(reader: OpenCSVReader) extends Iterator[Row] {
    private[this] val rows: Iterator[Row] = new CSVRowIterator(reader).flatten

    override def hasNext(): Boolean = rows.hasNext
    override def next(): Row = rows.next()

    def apply[T](fn: Row => T): Iterator[T] = {
      this.map { fn }
    }

    def close() {
      reader.close()
    }
  }


  /**
   * Wrapper class for OpenCSVReader to allow for Thread-safe CSV row iteration.
   * Note: This class is actually an iterator over Option[Row]. This is to
   * allow for safer/easy handling of cases where the the rows are null.
   */
  class CSVRowIterator(reader: OpenCSVReader) extends Iterator[Option[Row]] {
    var nextLine: Option[Row] = Option(reader.readNext())

    override def hasNext() = nextLine.isDefined

    override def next(): Option[Row] = {
      val cur: Option[Row] = nextLine
      nextLine = Option(reader.readNext())
      cur
    }

    /** converts to Iterator[Row] */
    def asRows(): Iterator[Row] = {
      this.flatten
    }

    /** alias for mapping */
    def apply[T](fn: Row => T): Iterator[T] = {
      this.flatten.map { fn }
    }

    /** closes reader */
    def close() {
      reader.close()
    }
  }

  /** 
   * Allows for writing rows with Map[String,String] objects.
   * headers -- specifies the list of values to extract from Map[String,String] objects.
   *            Also specifies the column ordering of the output.
   */
  class CSVDictWriter(writer: OpenCSVWriter, headers: Seq[String]) {
    
    /** writes the header */
    def writeHeader() { writer.writeNext(headers.toArray) }
    
    /** writes a row */
    def write(row: Map[String, String]) { 
      val rowData: Array[String] = headers.map { col: String =>
        row.get(col) getOrElse sys.error("Column (%s) not found in row [%s]".format(col, row.toString))
      }.toArray
      
      writer.writeNext(rowData)
    }

    def close() { writer.close() }

    def flush() { writer.synchronized { writer.flush() } }
  }

}


