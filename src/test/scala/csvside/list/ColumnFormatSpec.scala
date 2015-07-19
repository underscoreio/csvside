package csvside
package list

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

class ColumnFormatSpec extends FreeSpec with Matchers {
  val validRow = Map(
    "Column 1" -> "abc",
    "Column 2" -> "123",
    "Column 3" -> "yes",
    "Column 4" -> ""
  )

  val invalidRow = Map(
    "Column 1" -> "abc",
    "Column 2" -> "abc",
    "Column 3" -> "abc",
    "Column 4" -> "abc"
  )

  val emptyRow = Map(
    "Column 1" -> "",
    "Column 2" -> "",
    "Column 3" -> "",
    "Column 4" -> ""
  )

  val format1 = "Column 1".as[String]
  val format2 = "Column 2".as[Int]
  val format3 = "Column 3".as[Boolean]
  val format4 = "Column 4".as[Option[Double]]

  case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

  val testFormat: ColumnFormat[Test] = (
    "Column 1".as[String] |@|
    "Column 2".as[Int] |@|
    "Column 3".as[Boolean] |@|
    "Column 4".as[Option[Double]]
  ) map (Test.apply)

  "as[A]" - {
    "valid" in {
      format1(validRow) should equal(valid("abc"))
      format2(validRow) should equal(valid(123))
      format3(validRow) should equal(valid(true))
      format4(validRow) should equal(valid(None))
    }

    "invalid" in {
      format1(invalidRow) should equal(valid("abc"))
      format2(invalidRow) should equal(invalid(List("Column 2: Must be a whole number")))
      format3(invalidRow) should equal(invalid(List("Column 3: Must be a yes/no value")))
      format4(invalidRow) should equal(invalid(List("Column 4: Must be a number or blank")))
    }

    "empty" in {
      format1(emptyRow) should equal(valid(""))
      format2(emptyRow) should equal(invalid(List("Column 2: Must be a whole number")))
      format3(emptyRow) should equal(invalid(List("Column 3: Must be a yes/no value")))
      format4(emptyRow) should equal(valid(None))
    }
  }

  "applicative" - {
    "valid" in {
      testFormat(validRow) should equal(valid(Test(
        "abc",
        123,
        true,
        None
      )))
    }

    "invalid" in {
      testFormat(invalidRow) should equal(invalid(List(
        "Column 2: Must be a whole number",
        "Column 3: Must be a yes/no value",
        "Column 4: Must be a number or blank"
      )))
    }

    "empty" in {
      testFormat(emptyRow) should equal(invalid(List(
        "Column 2: Must be a whole number",
        "Column 3: Must be a yes/no value"
      )))
    }
  }
}
