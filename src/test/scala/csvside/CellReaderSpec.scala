package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._

import org.scalatest._

class CellReaderSpec extends FreeSpec with Matchers with CellReaders {
  "stringReader" - {
    val format = implicitly[CellReader[String]]

    "valid" in {
      format(CsvCell(1, "Col", "Hi")) should equal(valid("Hi"))
    }
  }

  "intReader" - {
    val format = implicitly[CellReader[Int]]

    "valid" in {
      format(CsvCell(1, "Col", "123")) should equal(valid(123))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "123.4")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }

  "longReader" - {
    val format = implicitly[CellReader[Long]]

    "valid" in {
      format(CsvCell(1, "Col", "123")) should equal(valid(123L))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "123.4")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }

  "doubleReader" - {
    val format = implicitly[CellReader[Double]]

    "valid" in {
      format(CsvCell(1, "Col", "123")) should equal(valid(123.0))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a number"))))
    }
  }

  "optionReader" - {
    val format = implicitly[CellReader[Option[Int]]]

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

  "cellReader.map" - {
    case class Id(value: Int)
    val format = implicitly[CellReader[Int]].map(Id(_))

    "valid" in {
      format(CsvCell(1, "Col", "123")) should be(valid(Id(123)))
    }

    "invalid" in {
      format(CsvCell(1, "Col", "abc")) should be(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }
}
