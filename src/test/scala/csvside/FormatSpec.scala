package csvside

import cats.data.Validated
import cats.data.Validated.{Valid, Invalid}
import cats.implicits._


import unindent._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FormatSpec extends AnyFreeSpec with Matchers {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testFormat: RowFormat[Test] = (
    "Str".csv[String],
    "Int".csv[Int],
    "Bool".csv[Option[Boolean]]
  ).imapN(Test.apply)(unlift(Test.unapply))

  "valid read" in {
    val csv = i"""
      Str,Bool,Int
      abc,true,123
      "a b",false,321
      """

    Csv.fromString[Test](csv).toList should equal(List(
      CsvSuccess(2, "abc,true,123", Test("abc", 123, Some(true))),
      CsvSuccess(3, "a b,false,321", Test("a b", 321, Some(false)))
    ))
  }

  "invalid read" in {
    val csv = i"""
      Str,Bool,Int
      ,,
      abc,abc,abc
      """

    Csv.fromString[Test](csv).toList should equal(List(
      CsvFailure(2, ",,", List(
        CsvError(CsvPath("Int"), "Must be a whole number")
      )),
      CsvFailure(3, "abc,abc,abc", List(
        CsvError(CsvPath("Int"), "Must be a whole number"),
        CsvError(CsvPath("Bool"), "Must be a yes/no value or blank")
      ))
    ))
  }

  "write" in {
    Csv.toString(Seq(
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
      if(n > 0) Valid(n * 2) else Invalid("Must be > 0")

    def halve(n: Int): Int =
      n / 2

    val validatedTestFormat: RowFormat[Test] = (
      "Str".csv[String],
      "Int".csv[Int].ivalidate(validateAndDouble, halve),
      "Bool".csv[Option[Boolean]]
    ).imapN(Test.apply)(unlift(Test.unapply))

    val validatedTestReader =
      ListReader.fromRowReader(validatedTestFormat)

    "should transform valid values" in {
      val csv = i"""
        Str,Int,Bool
        abc,123,true
        """

      Csv.fromString[Test](csv)(validatedTestReader).toList should equal(List(
        CsvSuccess(2, "abc,123,true", Test("abc", 246, Some(true)))
      ))
    }

    "should pass through errors" in {
      val csv = i"""
        Str,Int,Bool
        abc,,true
        """

      Csv.fromString[Test](csv)(validatedTestReader).toList should equal(List(
        CsvFailure(2, "abc,,true", List(CsvError(CsvPath("Int"), "Must be a whole number")))
      ))
    }

    "should fail when validation rule fails" in {
      val csv = i"""
        Str,Int,Bool
        abc,-123,true
        """

      Csv.fromString[Test](csv)(validatedTestReader).toList should equal(List(
        CsvFailure(2, "abc,-123,true", List(CsvError(CsvPath("Int"), "Must be > 0")))
      ))
    }

    "should tranform written values" in {
      Csv.toString(Seq(
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
