package csvside

import scala.language.implicitConversions

case class CsvPath(parts: Seq[String]) {
  def prefix(str: String): CsvPath =
    CsvPath(str +: parts)

  def prefix(that: CsvPath): CsvPath =
    CsvPath(that.parts ++ this.parts)

  lazy val text: String =
    parts mkString (": ")
}

object CsvPath {
  val empty: CsvPath = CsvPath(Seq())
  val emptyList: List[CsvPath] = List()

  def apply(str: String): CsvPath =
    CsvPath(Seq(str))
}