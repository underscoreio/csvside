package csvside

import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RowWriterSpec extends AnyFreeSpec with Matchers {
  val validRow = CsvRow(1, Map(
    CsvPath("Column 1") -> "abc",
    CsvPath("Column 2") -> "123",
    CsvPath("Column 3") -> "yes",
    CsvPath("Column 4") -> ""
  ))

  val invalidRow = CsvRow(2, Map(
    CsvPath("Column 1") -> "abc",
    CsvPath("Column 2") -> "abc",
    CsvPath("Column 3") -> "abc",
    CsvPath("Column 4") -> "abc"
  ))

  val emptyRow = CsvRow(3, Map(
    CsvPath("Column 1") -> "",
    CsvPath("Column 2") -> "",
    CsvPath("Column 3") -> "",
    CsvPath("Column 4") -> ""
  ))

  val writer1 = "Column 1".write[String]
  val writer2 = "Column 2".write[Int]
  val writer3 = "Column 3".write[Boolean]
  val writer4 = "Column 4".write[Option[Double]]

  case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

  val testWriter: RowWriter[Test] =
    "Column 1".write[String].contramap[Test](_.a) ~
    "Column 2".write[Int].contramap[Test](_.b) ~
    "Column 3".write[Boolean].contramap[Test](_.c) ~
    "Column 4".write[Option[Double]].contramap[Test](_.d)

  "writeConstant[A]" - {
    val data = new Exception("Foo")
    implicit val exceptionWriter = CellWriter[Exception](_.getMessage)
    val writer = "Column 1".writeConstant[Exception](data)

    "valid" in {
      writer.write(data, 1) should equal(CsvRow(1, Map(CsvPath("Column 1") -> "Foo")))
    }
  }

  "single writers" in {
    writer1.write("abc", 1) should equal(CsvRow(1, Map(CsvPath("Column 1") -> "abc")))
    writer2.write(123  , 1) should equal(CsvRow(1, Map(CsvPath("Column 2") -> "123")))
    writer3.write(true , 1) should equal(CsvRow(1, Map(CsvPath("Column 3") -> "true")))
    writer4.write(None , 1) should equal(CsvRow(1, Map(CsvPath("Column 4") -> "")))
  }

  "compound writer" in {
    testWriter.write(Test("abc", 123, true, None), 1) should equal(CsvRow(1, Map(
      CsvPath("Column 1") -> "abc",
      CsvPath("Column 2") -> "123",
      CsvPath("Column 3") -> "true",
      CsvPath("Column 4") -> ""
    )))
  }
}
