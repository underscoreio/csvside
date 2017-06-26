package csvside

import cats.data.Validated
import cats.data.Validated.{valid, invalid}
import cats.instances.all._

import org.scalatest._

class CellWriterSpec extends FreeSpec with Matchers with CellWriters {
  "stringWriter" in {
    val writer = implicitly[CellWriter[String]]
    writer("Hi") should equal("Hi")
  }

  "intWriter" in {
    val writer = implicitly[CellWriter[Int]]
    writer(123) should equal("123")
  }

  "longWriter" in {
    val writer = implicitly[CellWriter[Long]]
    writer(123L) should equal("123")
  }

  "doubleWriter" in {
    val writer = implicitly[CellWriter[Double]]
    writer(123.0) should equal("123.0")
  }

  "optionWriter" in {
    val writer = implicitly[CellWriter[Option[Int]]]
    writer(Some(123)) should equal("123")
    writer(None) should equal("")
  }

  "cellWriter.map" in {
    case class Id(value: Int)
    val writer = implicitly[CellWriter[Int]].contramap[Id](_.value)
    writer(Id(123)) should be("123")
  }
}
