package csvside

import cats.data.Validated
import cats.data.Validated.{Valid, Invalid}
import cats.implicits._

import org.scalatest._

import unindent._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ReadSpec extends AnyFreeSpec with Matchers {
  "Csv.fromString" - {
    "using RowReader" - {
      "valid" in new RowReaderFixtures {
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

      "invalid" in new RowReaderFixtures {
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
    }

    "using ListReader" - {
      "valid" in new ListReaderFixtures {
        val csv = i"""
          Key,A,B,C
          x,1,2,3
          y,,,
          z,123,,456
          """

        Csv.fromString[Test](csv).toList should equal(List(
          CsvSuccess(2, "x,1,2,3", Test("x", Map("A" -> Some(1), "B" -> Some(2), "C" -> Some(3)))),
          CsvSuccess(3, "y,,,", Test("y", Map("A" -> None, "B" -> None, "C" -> None))),
          CsvSuccess(4, "z,123,,456", Test("z", Map("A" -> Some(123), "B" -> None, "C" -> Some(456))))
        ))
      }

      "invalid header row" in new ListReaderFixtures {
        val csv = i"""
          Badness,A,B,C
          """

        Csv.fromString[Test](csv).toList should equal(List(
          CsvFailure(1, "Badness,A,B,C", List(CsvError(CsvPath(""), "Invalid header row")))
        ))
      }

      "invalid data rows" in new ListReaderFixtures {
        val csv = i"""
          Key,A,B,C
          x,badness,2,3
          z,,badness,alsobadness
          """

        Csv.fromString[Test](csv).toList should equal(List(
          CsvFailure(2, "x,badness,2,3", List(
            CsvError(CsvPath("A"), "Must be a whole number or blank")
          )),
          CsvFailure(3, "z,,badness,alsobadness", List(
            CsvError(CsvPath("B"), "Must be a whole number or blank"),
            CsvError(CsvPath("C"), "Must be a whole number or blank")
          ))
        ))
      }
    }
  }

  "validate" - {
    def validateAndDouble(n: Int): Validated[String, Int] =
      if(n > 0) Valid(n * 2)else Invalid("Must be > 0")

    case class Test(a: String, b: Int, c: Option[Boolean])

    implicit val testReader: RowReader[Test] = (
      "Str".read[String],
      "Int".read[Int].validate(validateAndDouble),
      "Bool".read[Option[Boolean]]
    ).mapN(Test.apply)

    "should transform valid values" in {

      val csv = i"""
        Str,Int,Bool
        abc,123,true
        """

      Csv.fromString[Test](csv).toList should equal(List(
        CsvSuccess(2, "abc,123,true", Test("abc", 246, Some(true)))
      ))
    }

    "should pass through errors" in {
      val csv = i"""
        Str,Int,Bool
        abc,,true
        """

      Csv.fromString[Test](csv).toList should equal(List(
        CsvFailure(2, "abc,,true", List(CsvError(CsvPath("Int"), "Must be a whole number")))
      ))
    }

    "should fail when validation rule fails" in {
      val csv = i"""
        Str,Int,Bool
        abc,-123,true
        """

      Csv.fromString[Test](csv).toList should equal(List(
        CsvFailure(2, "abc,-123,true", List(CsvError(CsvPath("Int"), "Must be > 0")))
      ))
    }
  }
}

trait RowReaderFixtures {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testReader: RowReader[Test] = (
    "Str".read[String],
    "Int".read[Int],
    "Bool".read[Option[Boolean]]
  ).mapN(Test)
}

trait ListReaderFixtures {
  case class Test(a: String, b: Map[String, Option[Int]])

  implicit val testReader: ListReader[Test] =
    ListReader[Test] {
      case "Key" :: tail =>
        Valid(("Key".read[String], tail.readMap[Option[Int]]).mapN(Test.apply))

      case cells =>
        Invalid(List(CsvError(CsvPath(""), s"Invalid header row")))
    }
}
