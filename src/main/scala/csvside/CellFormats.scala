package csvside

import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait CellFormats {
  implicit val stringFormat: CellFormat[String] =
    CellFormat[String] { cell =>
      valid(cell.value)
    }

  implicit val intFormat: CellFormat[Int] =
    CellFormat[Int] { cell =>
      try {
        valid(cell.value.toInt)
      } catch {
        case exn: NumberFormatException =>
          invalid(List(cell.error("Must be a whole number")))
      }
    }

  implicit val doubleFormat: CellFormat[Double] =
    CellFormat[Double] { cell =>
      try {
        valid(cell.value.toDouble)
      } catch {
        case exn: NumberFormatException =>
          invalid(List(cell.error("Must be a number")))
      }
    }

  implicit val booleanFormat: CellFormat[Boolean] =
    CellFormat[Boolean] { cell =>
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

  implicit def optionFormat[A](implicit format: CellFormat[A]): CellFormat[Option[A]] = {
    def suffix(errors: List[CsvError]): List[CsvError] =
      errors.map(error => error.copy(message = error.message + " or blank"))

    CellFormat[Option[A]] { cell =>
      cell.value.trim match {
        case "" => valid(None)
        case _  => format(cell).bimap(suffix, Some(_))
      }
    }
  }
}