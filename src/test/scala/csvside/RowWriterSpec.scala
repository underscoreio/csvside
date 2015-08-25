package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

class RowWriterSpec extends FreeSpec with Matchers {
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

  val writer1 = "Column 1".write[String]
  val writer2 = "Column 2".write[Int]
  val writer3 = "Column 3".write[Boolean]
  val writer4 = "Column 4".write[Option[Double]]

  case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

  val testWriter: RowWriter[Test] =
    "Column 1".write[String].contramap[Test](_.a) ~
    "Column 2".write[Int].contramap[Test](_.b) ~
    "Column 3".write[Boolean].contramap[Test](_.c) ~
    "Column 4".write[Option[Double]].contramap[Test](_.d)

  // "constant[A]" - {
  //   val data   = new Exception("WOO!")
  //   val writer = constant[Exception](data)

  //   "valid" in {
  //     writer(emptyRow) should equal(valid(data))
  //   }
  // }

  // "as[A]" - {
  //   "valid" in {
  //     writer1(validRow) should equal(valid("abc"))
  //     writer2(validRow) should equal(valid(123))
  //     writer3(validRow) should equal(valid(true))
  //     writer4(validRow) should equal(valid(None))
  //   }

  //   "invalid" in {
  //     writer1(invalidRow) should equal(valid("abc"))
  //     writer2(invalidRow) should equal(invalid(List(CsvError(2, "Column 2", "Must be a whole number"))))
  //     writer3(invalidRow) should equal(invalid(List(CsvError(2, "Column 3", "Must be a yes/no value"))))
  //     writer4(invalidRow) should equal(invalid(List(CsvError(2, "Column 4", "Must be a number or blank"))))
  //   }

  //   "empty" in {
  //     writer1(emptyRow) should equal(valid(""))
  //     writer2(emptyRow) should equal(invalid(List(CsvError(3, "Column 2", "Must be a whole number"))))
  //     writer3(emptyRow) should equal(invalid(List(CsvError(3, "Column 3", "Must be a yes/no value"))))
  //     writer4(emptyRow) should equal(valid(None))
  //   }
  // }

  // "applicative" - {
  //   "valid" in {
  //     testWriter(validRow) should equal(valid(Test(
  //       "abc",
  //       123,
  //       true,
  //       None
  //     )))
  //   }

  //   "invalid" in {
  //     testWriter(invalidRow) should equal(invalid(List(
  //       CsvError(2, "Column 2", "Must be a whole number"),
  //       CsvError(2, "Column 3", "Must be a yes/no value"),
  //       CsvError(2, "Column 4", "Must be a number or blank")
  //     )))
  //   }

  //   "empty" in {
  //     testWriter(emptyRow) should equal(invalid(List(
  //       CsvError(3, "Column 2", "Must be a whole number"),
  //       CsvError(3, "Column 3", "Must be a yes/no value")
  //     )))
  //   }
  // }
}
