package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.cartesian._
import cats.syntax.validated._

import org.scalatest._

import unindent._

class ReadSpec extends FreeSpec with Matchers {
  "read(string)" - {
    "using RowReader" - {
      "valid" in new RowReaderFixtures {
        val csv = i"""
          Str,Bool,Int
          abc,true,123
          "a b",false,321
          """

        read[Test](csv).toList should equal(List(
          valid(Test("abc", 123, Some(true))),
          valid(Test("a b", 321, Some(false)))
        ))
      }

      "invalid" in new RowReaderFixtures {
        val csv = i"""
          Str,Bool,Int
          ,,
          abc,abc,abc
          """

        read[Test](csv).toList should equal(List(
          invalid(Seq(
            CsvError(2, CsvPath("Int"), "Must be a whole number")
          )),
          invalid(Seq(
            CsvError(3, CsvPath("Int"), "Must be a whole number"),
            CsvError(3, CsvPath("Bool"), "Must be a yes/no value or blank")
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

        read[Test](csv).toList should equal(List(
          valid(Test("x", Map("A" -> Some(1), "B" -> Some(2), "C" -> Some(3)))),
          valid(Test("y", Map("A" -> None, "B" -> None, "C" -> None))),
          valid(Test("z", Map("A" -> Some(123), "B" -> None, "C" -> Some(456))))
        ))
      }

      "invalid header row" in new ListReaderFixtures {
        val csv = i"""
          Badness,A,B,C
          """

        read[Test](csv).toList should equal(List(
          invalid(List(CsvError(1, CsvPath(""), s"Bad header row: Badness, A, B, C")))
        ))
      }

      "invalid data rows" in new ListReaderFixtures {
        val csv = i"""
          Key,A,B,C
          x,badness,2,3
          z,,badness,alsobadness
          """

        read[Test](csv).toList should equal(List(
          invalid(Seq(
            CsvError(2, CsvPath("A"), "Must be a whole number or blank")
          )),
          invalid(Seq(
            CsvError(3, CsvPath("B"), "Must be a whole number or blank"),
            CsvError(3, CsvPath("C"), "Must be a whole number or blank")
          ))
        ))
      }
    }
  }

  "validate" - {
    def validateAndDouble(n: Int): Validated[String, Int] =
      if(n > 0) (n * 2).valid else "Must be > 0".invalid

    case class Test(a: String, b: Int, c: Option[Boolean])

    implicit val testReader: RowReader[Test] = (
      "Str".read[String] |@|
      "Int".read[Int].validate(validateAndDouble) |@|
      "Bool".read[Option[Boolean]]
    ) map (Test.apply)

    "should transform valid values" in {

      val csv = i"""
        Str,Int,Bool
        abc,123,true
        """

      read[Test](csv).toList should equal(List(
        valid(Test("abc", 246, Some(true)))
      ))
    }

    "should pass through errors" in {
      val csv = i"""
        Str,Int,Bool
        abc,,true
        """

      read[Test](csv).toList should equal(List(
        invalid(Seq(CsvError(2, CsvPath("Int"), "Must be a whole number")))
      ))
    }

    "should fail when validation rule fails" in {
      val csv = i"""
        Str,Int,Bool
        abc,-123,true
        """

      read[Test](csv).toList should equal(List(
        Seq(CsvError(2, CsvPath("Int"), "Must be > 0")).invalid
      ))
    }
  }
}

trait RowReaderFixtures {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testReader: RowReader[Test] = (
    "Str".read[String] |@|
    "Int".read[Int] |@|
    "Bool".read[Option[Boolean]]
  ) map (Test.apply)
}

trait ListReaderFixtures {
  case class Test(a: String, b: Map[String, Option[Int]])

  implicit val testReader: ListReader[Test] =
    ListReader[Test] {
      case "Key" :: tail =>
        (("Key".read[String] |@| tail.readMap[Option[Int]]) map (Test.apply)).valid

      case cells =>
        List(CsvError(1, CsvPath(""), s"Bad header row: ${cells.mkString(", ")}")).invalid
    }
}