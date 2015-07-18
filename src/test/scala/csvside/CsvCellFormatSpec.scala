package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._

import org.scalatest._

class CsvCellFormatSpec extends FreeSpec with Matchers {
  "stringFormat" - {
    val format = implicitly[CsvCellFormat[String]]

    "valid" in {
      format("Hi") should equal(valid("Hi"))
    }
  }

  "intFormat" - {
    val format = implicitly[CsvCellFormat[Int]]

    "valid" in {
      format("123") should equal(valid(123))
    }

    "invalid" in {
      format("123.4") should equal(invalid(List("Must be a whole number")))
      format("abc") should equal(invalid(List("Must be a whole number")))
    }
  }

  "doubleFormat" - {
    val format = implicitly[CsvCellFormat[Double]]

    "valid" in {
      format("123") should equal(valid(123.0))
    }

    "invalid" in {
      format("abc") should equal(invalid(List("Must be a number")))
    }
  }

  "optionFormat" - {
    val format = implicitly[CsvCellFormat[Option[Int]]]

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
