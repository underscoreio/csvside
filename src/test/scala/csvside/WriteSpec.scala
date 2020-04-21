package csvside

import cats.implicits._

import org.scalatest._

import unindent._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class WriteSpec extends AnyFreeSpec with Matchers {
  case class Test(a: String, b: Int, c: Option[Boolean])

  implicit val testWriter: RowWriter[Test] = (
    "Str".write[String],
    "Int".write[Int],
    "Bool".write[Option[Boolean]]
  ) contramapN (unlift(Test.unapply))

  "Csv.toString" in {
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
}
