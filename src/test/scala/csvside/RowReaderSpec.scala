package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

class RowReaderSpec extends FreeSpec with Matchers {
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

  val reader1 = "Column 1".read[String]
  val reader2 = "Column 2".read[Int]
  val reader3 = "Column 3".read[Boolean]
  val reader4 = "Column 4".read[Option[Double]]

  case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

  val testReader: RowReader[Test] = (
    "Column 1".read[String]  |@|
    "Column 2".read[Int]     |@|
    "Column 3".read[Boolean] |@|
    "Column 4".read[Option[Double]]
  ) map (Test.apply)

  "readConstant[A]" - {
    val data   = new Exception("WOO!")
    val reader = readConstant[Exception](data)

    "valid" in {
      reader(emptyRow) should equal(valid(data))
    }
  }

  "read[A]" - {
    "valid" in {
      reader1(validRow) should equal(valid("abc"))
      reader2(validRow) should equal(valid(123))
      reader3(validRow) should equal(valid(true))
      reader4(validRow) should equal(valid(None))
    }

    "invalid" in {
      reader1(invalidRow) should equal(valid("abc"))
      reader2(invalidRow) should equal(invalid(List(CsvError(2, "Column 2", "Must be a whole number"))))
      reader3(invalidRow) should equal(invalid(List(CsvError(2, "Column 3", "Must be a yes/no value"))))
      reader4(invalidRow) should equal(invalid(List(CsvError(2, "Column 4", "Must be a number or blank"))))
    }

    "empty" in {
      reader1(emptyRow) should equal(valid(""))
      reader2(emptyRow) should equal(invalid(List(CsvError(3, "Column 2", "Must be a whole number"))))
      reader3(emptyRow) should equal(invalid(List(CsvError(3, "Column 3", "Must be a yes/no value"))))
      reader4(emptyRow) should equal(valid(None))
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
