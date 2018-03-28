package csvside

import cats.syntax.validated._
import scala.util.matching.Regex

trait CellReaders {
  implicit val stringReader: CellReader[String] =
    CellReader[String] { value => value.trim.valid }

  def regexReader(regex: Regex, msg: String): CellReader[String] =
    CellReader[String] { value =>
      val trimmed = value.trim

      // We don't use pattern matching here because
      // we don't know how many capturing groups the user has placed in regex:
      if(regex.pattern.matcher(trimmed).matches) trimmed.valid else msg.invalid
    }

  private def numericReader[A](func: String => A)(msg: String): CellReader[A] =
    CellReader[A] { value =>
      try {
        func(value.trim).valid
      } catch {
        case exn: NumberFormatException =>
          msg.invalid
      }
    }

  implicit val intReader: CellReader[Int] =
    numericReader(_.toInt)("Must be a whole number")

  implicit val longReader: CellReader[Long] =
    numericReader(_.toLong)("Must be a whole number")

  implicit val doubleReader: CellReader[Double] =
    numericReader(_.toDouble)("Must be a number")

  implicit val booleanReader: CellReader[Boolean] =
    CellReader[Boolean] { value =>
      value.trim.toLowerCase match {
        case "true"  => true.valid
        case "false" => false.valid
        case "yes"   => true.valid
        case "no"    => false.valid
        case "y"     => true.valid
        case "n"     => false.valid
        case "t"     => true.valid
        case "f"     => false.valid
        case "1"     => true.valid
        case "0"     => false.valid
        case _       => "Must be a yes/no value".invalid
      }
    }

  implicit def optionReader[A](implicit reader: CellReader[A]): CellReader[Option[A]] = {
    CellReader[Option[A]] { value =>
      value.trim match {
        case "" => None.valid
        case _  => reader(value).bimap(_ + " or blank", Some(_))
      }
    }
  }
}
