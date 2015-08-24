package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

class ColumnReaderSpec extends FreeSpec with Matchers {
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

  val format1 = "Column 1".as[String]
  val format2 = "Column 2".as[Int]
  val format3 = "Column 3".as[Boolean]
  val format4 = "Column 4".as[Option[Double]]

  case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

  val testReader: ColumnReader[Test] = (
    "Column 1".as[String] |@|
    "Column 2".as[Int] |@|
    "Column 3".as[Boolean] |@|
    "Column 4".as[Option[Double]]
  ) map (Test.apply)

  "constant[A]" - {
    val data   = new Exception("WOO!")
    val format = constant[Exception](data)

    "valid" in {
      format(emptyRow) should equal(valid(data))
    }
  }

  "as[A]" - {
    "valid" in {
      format1(validRow) should equal(valid("abc"))
      format2(validRow) should equal(valid(123))
      format3(validRow) should equal(valid(true))
      format4(validRow) should equal(valid(None))
    }

    "invalid" in {
      format1(invalidRow) should equal(valid("abc"))
      format2(invalidRow) should equal(invalid(List(CsvError(2, "Column 2", "Must be a whole number"))))
      format3(invalidRow) should equal(invalid(List(CsvError(2, "Column 3", "Must be a yes/no value"))))
      format4(invalidRow) should equal(invalid(List(CsvError(2, "Column 4", "Must be a number or blank"))))
    }

    "empty" in {
      format1(emptyRow) should equal(valid(""))
      format2(emptyRow) should equal(invalid(List(CsvError(3, "Column 2", "Must be a whole number"))))
      format3(emptyRow) should equal(invalid(List(CsvError(3, "Column 3", "Must be a yes/no value"))))
      format4(emptyRow) should equal(valid(None))
    }
  }

  "applicative" - {
    "valid" in {
      testReader(validRow) should equal(valid(Test(
        "abc",
        123,
        true,
        None
      )))
    }

    "invalid" in {
      testReader(invalidRow) should equal(invalid(List(
        CsvError(2, "Column 2", "Must be a whole number"),
        CsvError(2, "Column 3", "Must be a yes/no value"),
        CsvError(2, "Column 4", "Must be a number or blank")
      )))
    }

    "empty" in {
      testReader(emptyRow) should equal(invalid(List(
        CsvError(3, "Column 2", "Must be a whole number"),
        CsvError(3, "Column 3", "Must be a yes/no value")
      )))
    }
  }
}
