package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._

import org.scalatest._

class CellFormatSpec extends FreeSpec with Matchers with CellFormats {
  "stringFormat" - {
    val format = implicitly[CellFormat[String]]

    "valid" in {
      format("Hi") should equal(valid("Hi"))
    }
  }

  "intFormat" - {
    val format = implicitly[CellFormat[Int]]

    "valid" in {
      format("123") should equal(valid(123))
    }

    "invalid" in {
      format("123.4") should equal(invalid(List("Must be a whole number")))
      format("abc") should equal(invalid(List("Must be a whole number")))
    }
  }

  "doubleFormat" - {
    val format = implicitly[CellFormat[Double]]

    "valid" in {
      format("123") should equal(valid(123.0))
    }

    "invalid" in {
      format("abc") should equal(invalid(List("Must be a number")))
    }
  }

  "optionFormat" - {
    val format = implicitly[CellFormat[Option[Int]]]

    "present and valid" - {
      format("123") should equal(valid(Some(123)))
    }

    "present and invalid" - {
      format("abc") should equal(invalid(List("Must be a whole number or blank")))
    }

    "absent" - {
      format("") should equal(valid(None))
    }
  }
}
