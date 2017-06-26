package csvside

sealed abstract class CsvValidated[+A] extends Product with Serializable {
  def number: Int
  def text: String

  def map[B](func: A => B): CsvValidated[B] =
    this match {
      case CsvSuccess(number, text, value) => CsvSuccess(number, text, func(value))
      case CsvFailure(number, text, errors) => CsvFailure(number, text, errors)
    }

  def flatMap[B](func: A => CsvValidated[B]) =
    this match {
      case CsvSuccess(_, _, value) => func(value)
      case CsvFailure(number, text, errors) => CsvFailure(number, text, errors)
    }

  def fold[B](failure: (Int, String, List[CsvError]) => B, success: (Int, String, A) => B): B =
    this match {
      case CsvSuccess(number, text, value)  => success(number, text, value)
      case CsvFailure(number, text, errors) => failure(number, text, errors)
    }
}

final case class CsvSuccess[A](number: Int, text: String, value: A) extends CsvValidated[A]
final case class CsvFailure[A](number: Int, text: String, errors: List[CsvError]) extends CsvValidated[Nothing]
