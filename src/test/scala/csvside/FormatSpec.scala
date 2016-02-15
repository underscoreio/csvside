package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.cartesian._
import cats.syntax.validated._

import org.scalatest._

import unindent._

class FormatSpec extends FreeSpec with Matchers {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testFormat: RowFormat[Test] = (
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
        CsvError(2, CsvPath("Int"), "Must be a whole number")
      )),
      invalid(Seq(
        CsvError(3, CsvPath("Int"), "Must be a whole number"),
        CsvError(3, CsvPath("Bool"), "Must be a yes/no value or blank")
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

  "validate" - {
    def validateAndDouble(n: Int): Validated[String, Int] =
      if(n > 0) (n * 2).valid else "Must be > 0".invalid

    def halve(n: Int): Int =
      n / 2

    val validatedTestFormat: RowFormat[Test] = (
      "Str".csv[String] |@|
      "Int".csv[Int].ivalidate(validateAndDouble, halve) |@|
      "Bool".csv[Option[Boolean]]
    ).imap(Test.apply)(unlift(Test.unapply))

    val validatedTestReader =
      ListReader.fromRowReader(validatedTestFormat)

    "should transform valid values" in {
      val csv = i"""
        Str,Int,Bool
        abc,123,true
        """

      read[Test](csv)(validatedTestReader) should equal(Seq(
        valid(Test("abc", 246, Some(true)))
      ))
    }

    "should pass through errors" in {
      val csv = i"""
        Str,Int,Bool
        abc,,true
        """

      read[Test](csv)(validatedTestReader) should equal(Seq(
        invalid(Seq(CsvError(2, CsvPath("Int"), "Must be a whole number")))
      ))
    }

    "should fail when validation rule fails" in {
      val csv = i"""
        Str,Int,Bool
        abc,-123,true
        """

      read[Test](csv)(validatedTestReader) should equal(Seq(
        invalid(Seq(CsvError(2, CsvPath("Int"), "Must be > 0")))
      ))
    }

    "should tranform written values" in {
      csvString(Seq(
        Test("abc", 246, Some(true)),
        Test("def", 468, None)
      ))(validatedTestFormat) should equal {
        i"""
        "Str","Int","Bool"
        "abc","123","true"
        "def","234",""

        """
      }
    }
  }
}
