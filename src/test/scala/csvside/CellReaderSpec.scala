package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.std.all._

import org.scalatest._

class CellReaderSpec extends FreeSpec with Matchers with CellReaders {
  "stringReader" - {
    val reader = implicitly[CellReader[String]]

    "valid" in {
      reader(CsvCell(1, "Col", "Hi")) should equal(valid("Hi"))
    }
  }

  "intReader" - {
    val reader = implicitly[CellReader[Int]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123))
    }

    "invalid" in {
      reader(CsvCell(1, "Col", "123.4")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
      reader(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }

  "longReader" - {
    val reader = implicitly[CellReader[Long]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123L))
    }

    "invalid" in {
      reader(CsvCell(1, "Col", "123.4")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
      reader(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }

  "doubleReader" - {
    val reader = implicitly[CellReader[Double]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123.0))
    }

    "invalid" in {
      reader(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a number"))))
    }
  }

  "optionReader" - {
    val reader = implicitly[CellReader[Option[Int]]]

    "present and valid" - {
      reader(CsvCell(1, "Col", "123")) should equal(valid(Some(123)))
    }

    "present and invalid" - {
      reader(CsvCell(1, "Col", "abc")) should equal(invalid(List(CsvError(1, "Col", "Must be a whole number or blank"))))
    }

    "absent" - {
      reader(CsvCell(1, "Col", "") )should equal(valid(None))
    }
  }

  "cellReader.map" - {
    case class Id(value: Int)
    val reader = implicitly[CellReader[Int]].map(Id(_))

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should be(valid(Id(123)))
    }

    "invalid" in {
      reader(CsvCell(1, "Col", "abc")) should be(invalid(List(CsvError(1, "Col", "Must be a whole number"))))
    }
  }
}
