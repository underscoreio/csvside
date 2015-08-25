package csvside

import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait CellReaders {
  implicit val stringReader: CellReader[String] =
    CellReader[String] { cell =>
      valid(cell.value)
    }

  private def numericReader[A](func: String => A)(msg: String): CellReader[A] =
    CellReader[A] { cell =>
      try {
        valid(func(cell.value))
      } catch {
        case exn: NumberFormatException =>
          invalid(List(cell.error(msg)))
      }
    }

  implicit val intReader: CellReader[Int] =
    numericReader(_.toInt)("Must be a whole number")

  implicit val longReader: CellReader[Long] =
    numericReader(_.toLong)("Must be a whole number")

  implicit val doubleReader: CellReader[Double] =
    numericReader(_.toDouble)("Must be a number")

  implicit val booleanReader: CellReader[Boolean] =
    CellReader[Boolean] { cell =>
      cell.value.toLowerCase match {
        case "true" => valid(true)
        case "false" => valid(false)
        case "yes" => valid(true)
        case "no" => valid(false)
        case "y" => valid(true)
        case "n" => valid(false)
        case "t" => valid(true)
        case "f" => valid(false)
        case "1" => valid(true)
        case "0" => valid(false)
        case _ => invalid(List(cell.error("Must be a yes/no value")))
      }
    }

  implicit def optionReader[A](implicit reader: CellReader[A]): CellReader[Option[A]] = {
    def suffix(errors: List[CsvError]): List[CsvError] =
      errors.map(error => error.copy(message = error.message + " or blank"))

    CellReader[Option[A]] { cell =>
      cell.value.trim match {
        case "" => valid(None)
        case _  => reader(cell).bimap(suffix, Some(_))
      }
    }
  }
}