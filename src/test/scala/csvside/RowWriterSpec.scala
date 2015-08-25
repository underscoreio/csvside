package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

class RowWriterSpec extends FreeSpec with Matchers {
  val validRow = CsvRow(1, Map(
    "Column 1" -> "abc",
    "Column 2" -> "123",
    "Column 3" -> "yes",
    "Column 4" -> ""
  ))

  val invalidRow = CsvRow(2, Map(
    "Column 1" -> "abc",
    "Column 2" -> "abc",
    "Column 3" -> "abc",
    "Column 4" -> "abc"
  ))

  val emptyRow = CsvRow(3, Map(
    "Column 1" -> "",
    "Column 2" -> "",
    "Column 3" -> "",
    "Column 4" -> ""
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
      writer(data, 1) should equal(CsvRow(1, Map("Column 1" -> "Foo")))
    }
  }

  "single writers" in {
    writer1("abc", 1) should equal(CsvRow(1, Map("Column 1" -> "abc")))
    writer2(123  , 1) should equal(CsvRow(1, Map("Column 2" -> "123")))
    writer3(true , 1) should equal(CsvRow(1, Map("Column 3" -> "true")))
    writer4(None , 1) should equal(CsvRow(1, Map("Column 4" -> "")))
  }

  "compound writer" in {
    testWriter(Test("abc", 123, true, None), 1) should equal(CsvRow(1, Map(
      "Column 1" -> "abc",
      "Column 2" -> "123",
      "Column 3" -> "true",
      "Column 4" -> ""
    )))
  }
}
