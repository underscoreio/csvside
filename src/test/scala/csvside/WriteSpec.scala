package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.cartesian._

import org.scalatest._

import unindent._

class WriteSpec extends FreeSpec with Matchers {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testWriter: RowWriter[Test] = (
    "Str".write[String] |@|
    "Int".write[Int]    |@|
    "Bool".write[Option[Boolean]]
  ) contramap (unlift(Test.unapply))

  "csvString" in {
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
