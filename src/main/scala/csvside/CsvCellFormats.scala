package csvside

import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait CsvCellFormats {
  self: CsvTypes =>

  implicit val stringFormat: CsvCellFormat[String] =
    cell => valid(cell)

  implicit val intFormat: CsvCellFormat[Int] =
    cell => try {
      valid(cell.toInt)
    } catch {
      case exn: NumberFormatException =>
        invalid(List("Must be a whole number"))
    }

  implicit val doubleFormat: CsvCellFormat[Double] =
    cell => try {
      valid(cell.toDouble)
    } catch {
      case exn: NumberFormatException =>
        invalid(List("Must be a number"))
    }

  implicit val booleanFormat: CsvCellFormat[Boolean] =
    cell => cell.toLowerCase match {
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
      case _ => invalid(List("Must be a yes/no value"))
    }

  implicit def optionFormat[A](implicit format: CsvCellFormat[A]): CsvCellFormat[Option[A]] = {
    def suffix(errors: List[CsvError]): List[CsvError] =
      errors.map(_ + " or blank")

    cell => cell.trim match {
      case "" => valid(None)
      case _  => format(cell).bimap(suffix, Some(_))
    }
  }
}