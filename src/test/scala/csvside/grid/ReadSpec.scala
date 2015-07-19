package csvside
package grid

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.syntax.apply._

import org.scalatest._

import unindent._

class ReadSpec extends FreeSpec with Matchers {
  "read(string)" - {
    "valid" in {
      val csv = i"""
        ,A,B,C
        1,true,false,true
        2,false,,false
        3,true,false,true
        """

      read[Option[Boolean]](csv) should equal(valid(Map(
        ("A", "1") -> Some(true),
        ("A", "2") -> Some(false),
        ("A", "3") -> Some(true),
        ("B", "1") -> Some(false),
        ("B", "2") -> None,
        ("B", "3") -> Some(false),
        ("C", "1") -> Some(true),
        ("C", "2") -> Some(false),
        ("C", "3") -> Some(true)
      )))
    }

    "invalid" in {
      val csv = i"""
        ,A,B,C
        1,abc,false,true
        2,false,,false
        3,true,false,def
        """

      read[Option[Boolean]](csv) should equal(invalid(Seq(
        "A, 1: Must be a yes/no value or blank",
        "C, 3: Must be a yes/no value or blank"
      )))
    }
  }
}
