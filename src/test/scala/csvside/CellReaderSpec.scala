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

    "trimmed" in {
      reader(CsvCell(1, "Col", "  a b c  ")) should equal(valid("a b c"))
    }
  }

  "regexReader" - {
    val reader = regexReader("^[A-Z]$".r, "Must be a single uppercase letter")

    "valid" in {
      reader(CsvCell(1, "Col", "A")) should equal(valid("A"))
    }

    "invalid" in {
      val errors = invalid(List(CsvError(1, "Col", "Must be a single uppercase letter")))
      reader(CsvCell(1, "Col", "")) should equal(errors)
      reader(CsvCell(1, "Col", "a")) should equal(errors)
      reader(CsvCell(1, "Col", "AB")) should equal(errors)
    }

    "trimmed" in {
      reader(CsvCell(1, "Col", "  A  ")) should equal(valid("A"))
    }
  }

  "intReader" - {
    val reader = implicitly[CellReader[Int]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123))
    }

    "invalid" in {
      val errors = invalid(List(CsvError(1, "Col", "Must be a whole number")))
      reader(CsvCell(1, "Col", "123.4")) should equal(errors)
      reader(CsvCell(1, "Col", "abc")) should equal(errors)
    }

    "trimmed" in {
      reader(CsvCell(1, "Col", "  123  ")) should equal(valid(123))
    }
  }

  "longReader" - {
    val reader = implicitly[CellReader[Long]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123L))
    }

    "invalid" in {
      val errors = invalid(List(CsvError(1, "Col", "Must be a whole number")))
      reader(CsvCell(1, "Col", "123.4")) should equal(errors)
      reader(CsvCell(1, "Col", "abc")) should equal(errors)
    }

    "trimmed" in {
      reader(CsvCell(1, "Col", "  123  ")) should equal(valid(123L))
    }
  }

  "doubleReader" - {
    val reader = implicitly[CellReader[Double]]

    "valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(123.0))
    }

    "invalid" in {
      val errors = invalid(List(CsvError(1, "Col", "Must be a number")))
      reader(CsvCell(1, "Col", "abc")) should equal(errors)
    }

    "trimmed" in {
      reader(CsvCell(1, "Col", "  123.4  ")) should equal(valid(123.4))
    }
  }

  "optionReader" - {
    val reader = implicitly[CellReader[Option[Int]]]

    "present and valid" in {
      reader(CsvCell(1, "Col", "123")) should equal(valid(Some(123)))
    }

    "present and invalid" in {
      val errors = invalid(List(CsvError(1, "Col", "Must be a whole number or blank")))
      reader(CsvCell(1, "Col", "abc")) should equal(errors)
    }

    "absent" in {
      reader(CsvCell(1, "Col", "")) should equal(valid(None))
    }

    "trimmed" in {
      reader(CsvCell(1, "Col", "  123  ")) should equal(valid(Some(123)))
      reader(CsvCell(1, "Col", "    ")) should equal(valid(None))
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

    "trimmed" in {
      reader(CsvCell(1, "Col", "  123  ")) should equal(valid(Id(123)))
    }
  }
}
