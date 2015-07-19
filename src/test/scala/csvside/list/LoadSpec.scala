package csvside
package list

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

import unindent._

class LoadSpec extends FreeSpec with Matchers with LoadFixtures {
  "read(string)" - {
    "valid" in {
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

    "invalid" in {
      val csv = i"""
        Str,Bool,Int
        ,,
        abc,abc,abc
        """

      read[Test](csv) should equal(Seq(
        invalid(Seq(
          "Int: Must be a whole number"
        )),
        invalid(Seq(
          "Int: Must be a whole number",
          "Bool: Must be a yes/no value or blank"
        ))
      ))
    }
  }
}

trait LoadFixtures {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testFormat: ColumnFormat[Test] = (
    "Str".as[String] |@|
    "Int".as[Int] |@|
    "Bool".as[Option[Boolean]]
  ) map (Test.apply)
}