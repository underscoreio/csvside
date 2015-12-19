package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.monoidal._

import org.scalatest._

import unindent._

class FormatSpec extends FreeSpec with Matchers {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testWriter: RowFormat[Test] = (
    "Str".csv[String] |@|
    "Int".csv[Int]    |@|
    "Bool".csv[Option[Boolean]]
  ).imap(Test.apply)(unlift(Test.unapply))

  "valid read" in {
    val csv = i"""
      Str,Bool,Int
      abc,true,123
      "a b",false,321
      """

    read[Test](csv) should equal(Seq(
      valid(Test("abc", 123, Some(true))),
      valid(Test("a b", 321, Some(false)))
    ))
  }

  "invalid read" in {
    val csv = i"""
      Str,Bool,Int
      ,,
      abc,abc,abc
      """

    read[Test](csv) should equal(Seq(
      invalid(Seq(
        CsvError(2, "Int", "Must be a whole number")
      )),
      invalid(Seq(
        CsvError(3, "Int", "Must be a whole number"),
        CsvError(3, "Bool", "Must be a yes/no value or blank")
      ))
    ))
  }

  "write" in {
    csvString(Seq(
      Test("abc", 123, Some(true)),
      Test("a b", 321, Some(false)),
      Test("", 0, None)
    )) should equal {
      i"""
      "Str","Int","Bool"
      "abc","123","true"
      "a b","321","false"
      "","0",""

      """
    }
  }
}
