package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.cartesian._

import org.scalatest._

class RowReaderSpec extends FreeSpec with Matchers {
  val validRow = CsvRow(1, Map(
    CsvPath("Column 1") -> "abc",
    CsvPath("Column 2") -> "123",
    CsvPath("Column 3") -> "yes",
    CsvPath("Column 4") -> ""
  ))

  val invalidRow = CsvRow(2, Map(
    CsvPath("Column 1") -> "abc",
    CsvPath("Column 2") -> "abc",
    CsvPath("Column 3") -> "abc",
    CsvPath("Column 4") -> "abc"
  ))

  val emptyRow = CsvRow(3, Map(
    CsvPath("Column 1") -> "",
    CsvPath("Column 2") -> "",
    CsvPath("Column 3") -> "",
    CsvPath("Column 4") -> ""
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
      reader.read(emptyRow) should equal(valid(data))
    }
  }

  "read[A]" - {
    "valid" in {
      reader1.read(validRow) should equal(valid("abc"))
      reader2.read(validRow) should equal(valid(123))
      reader3.read(validRow) should equal(valid(true))
      reader4.read(validRow) should equal(valid(None))
    }

    "invalid" in {
      reader1.read(invalidRow) should equal(valid("abc"))
      reader2.read(invalidRow) should equal(invalid(List(CsvError(2, CsvPath("Column 2"), "Must be a whole number"))))
      reader3.read(invalidRow) should equal(invalid(List(CsvError(2, CsvPath("Column 3"), "Must be a yes/no value"))))
      reader4.read(invalidRow) should equal(invalid(List(CsvError(2, CsvPath("Column 4"), "Must be a number or blank"))))
    }

    "empty" in {
      reader1.read(emptyRow) should equal(valid(""))
      reader2.read(emptyRow) should equal(invalid(List(CsvError(3, CsvPath("Column 2"), "Must be a whole number"))))
      reader3.read(emptyRow) should equal(invalid(List(CsvError(3, CsvPath("Column 3"), "Must be a yes/no value"))))
      reader4.read(emptyRow) should equal(valid(None))
    }
  }

  "applicative" - {
    "valid" in {
      testReader.read(validRow) should equal(valid(Test(
        "abc",
        123,
        true,
        None
      )))
    }

    "invalid" in {
      testReader.read(invalidRow) should equal(invalid(List(
        CsvError(2, CsvPath("Column 2"), "Must be a whole number"),
        CsvError(2, CsvPath("Column 3"), "Must be a yes/no value"),
        CsvError(2, CsvPath("Column 4"), "Must be a number or blank")
      )))
    }

    "empty" in {
      testReader.read(emptyRow) should equal(invalid(List(
        CsvError(3, CsvPath("Column 2"), "Must be a whole number"),
        CsvError(3, CsvPath("Column 3"), "Must be a yes/no value")
      )))
    }
  }

  "readMap" - {
    "large number of columns" in {
      val columns = (0 to 10000).map(n => s"Column $n").toList
      val values  = (0 to 10000).map(n => s"Value $n").toList
      val row     = CsvRow(1, columns.map(CsvPath(_)).zip(values).toMap)

      val format = columns.readMap[String]

      format.read(row) should equal(Validated.valid(columns.zip(values).toMap))
    }
  }
}
