package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

import unindent._

class ReadSpec extends FreeSpec with Matchers {
  "read(string)" - {
    "using ColumnFormat" - {
      "valid" in new ColumnFormatFixtures {
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

      "invalid" in new ColumnFormatFixtures {
        val csv = i"""
          Str,Bool,Int
          ,,
          abc,abc,abc
          """

        read[Test](csv) should equal(Seq(
          invalid(Seq(
            "Line 2: Int: Must be a whole number"
          )),
          invalid(Seq(
            "Line 3: Int: Must be a whole number",
            "Line 3: Bool: Must be a yes/no value or blank"
          ))
        ))
      }
    }

    "using ListFormat" - {
      "valid" in new ListFormatFixtures {
        val csv = i"""
          Key,A,B,C
          x,1,2,3
          y,,,
          z,123,,456
          """

        read[Test](csv) should equal(Seq(
          valid(Test("x", Map("A" -> Some(1), "B" -> Some(2), "C" -> Some(3)))),
          valid(Test("y", Map("A" -> None, "B" -> None, "C" -> None))),
          valid(Test("z", Map("A" -> Some(123), "B" -> None, "C" -> Some(456))))
        ))
      }

      "invalid header row" in new ListFormatFixtures {
        val csv = i"""
          Badness,A,B,C
          """

        read[Test](csv) should equal(Seq(
          invalid(List(s"Line 1: Bad header row: Badness, A, B, C"))
        ))
      }

      "invalid data rows" in new ListFormatFixtures {
        val csv = i"""
          Key,A,B,C
          x,badness,2,3
          z,,badness,alsobadness
          """

        read[Test](csv) should equal(Seq(
          invalid(Seq(
            "Line 2: A: Must be a whole number or blank"
          )),
          invalid(Seq(
            "Line 3: B: Must be a whole number or blank",
            "Line 3: C: Must be a whole number or blank"
          ))
        ))
      }
    }
  }
}

trait ColumnFormatFixtures {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testFormat: ColumnFormat[Test] = (
    "Str".as[String] |@|
    "Int".as[Int] |@|
    "Bool".as[Option[Boolean]]
  ) map (Test.apply)
}

trait ListFormatFixtures {
  case class Test(a: String, b: Map[String, Option[Int]])

  implicit val testFormat: ListFormat[Test] =
    ListFormat[Test] {
      case "Key" :: tail =>
        valid(("Key".as[String] |@| tail.asMap[Option[Int]]) map (Test.apply))

      case cells =>
        invalid(List(s"Bad header row: ${cells.mkString(", ")}"))
    }
}