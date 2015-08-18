package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._

import org.scalatest._

class CellFormatSpec extends FreeSpec with Matchers with CellFormats {
  "stringFormat" - {
    val format = implicitly[CellFormat[String]]

    "valid" in {
      format(CsvCell(1, "Col", "Hi")) should equal(valid("Hi"))
    }
  }

  "intFormat" - {
    val format = implicitly[CellFormat[Int]]

    "valid" in {
      format(CsvCell(1, "Col", "123")) should equal(valid(123))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "123.4")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }

  "doubleFormat" - {
    val format = implicitly[CellFormat[Double]]

    "valid" in {
      format(CsvCell(1, "Col", "123")) should equal(valid(123.0))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a number"))))
    }
  }

  "optionFormat" - {
    val format = implicitly[CellFormat[Option[Int]]]

    "present and valid" - {
      format(CsvCell(1, "Col", "123")) should equal(valid(Some(123)))
    }

    "present and invalid" - {
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number or blank"))))
    }

    "absent" - {
      format(CsvCell(1, "Col", "") )should equal(valid(None))
    }
  }

  "cellFormat.map" - {
    case class Id(value: Int)
    val format = implicitly[CellFormat[Int]].map(Id(_))

    "valid" in {
      format(CsvCell(1, "Col", "123")) should be(valid(Id(123)))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "abc")) should be(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }
}
