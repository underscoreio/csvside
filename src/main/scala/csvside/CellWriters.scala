package csvside

import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait CellWriters {
  implicit val stringWriter: CellWriter[String] =
    CellWriter[String](identity)

  implicit val intWriter: CellWriter[Int] =
    CellWriter[Int](_.toString)

  implicit val longWriter: CellWriter[Long] =
    CellWriter[Long](_.toString)

  implicit val doubleWriter: CellWriter[Double] =
    CellWriter[Double](_.toString)

  implicit val booleanWriter: CellWriter[Boolean] =
    CellWriter[Boolean](if(_) "true" else "false")

  implicit def optionWriter[A](implicit writer: CellWriter[A]): CellWriter[Option[A]] =
    CellWriter[Option[A]](value => value map writer getOrElse "")
}