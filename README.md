# CSVSide

CSV readers and combinators for Scala. Made with Cats.

Copyright 2015 Richard Dallaway and Dave Gurnell. Licensed [Apache 2][license].

## Synopsis

CSVSide lets you read list-formatted and grid-formatted CSV files as follows.

### Fixed Row Format

Here's an example that reads directly from a `String`.
You can also read from a `java.io.File` or a `java.io.Reader`:

~~~ scala
import csvside._

// We want to parse this CSV...
val csv = """
  |Str,Bool,Int
  |abc,true,123
  |"a b",false,321
  |,,
  |abc,abc,abc
""".trim.stripMargin

// To a sequence of this data structure...
case class Test(str: String, num: Int, bool: Option[Boolean])

// We define a ColumnFormat...
implicit val testFormat: ColumnFormat[Test] = {
  import cats.syntax.apply._
  (
    "Str".as[String] |@|
    "Int".as[Int] |@|
    "Bool".as[Option[Boolean]]
  ) map (Test.apply)
}

// And read the data...
val ans = read[Test](csv)
// ans: Seq[Validated[Seq[String], Test]] =
//   Stream(
//     Valid(Test("abc",123,Some(true))),
//     Valid(Test("a b",321,Some(false))),
//     Invalid(List(
//       CsvError(3, "Int", "Must be a whole number")
//     )),
//     Invalid(List(
//       CsvError(4, "Int", "Must be a whole number"),
//       CsvError(4, "Bool", "Must be a yes/no value or blank")
//     ))
//   )
~~~

### Row Format Depends on Header Row

If the format of the rows depends on the values in the header row,
we can use a `ListFormat[A]` to generate a `ColumnFormat[A]` on the fly:

~~~ scala
import csvside._

// We want to parse the cells in this CSV...
val csv = i"""
  Row,Col1,Col2,Col3
  x,1,2,3
  y,,,
  z,3,,1
  """

// To a sequence of this data structure...
case class Test(key: String, values: Map[String, Int])

// We do this by creating a `ListFormat`,
// which parses the column headings and creates a `ColumnFormat`
// to read the rest of the file:

implicit val testFormat: ListFormat[Test] = {
  import cats.data.Validated.{valid, invalid}
  import cats.syntax.apply._
  ListFormat[Test] {
    case head :: tail =>
      valid((head.as[String] |@| tail.asMap[Option[Int]]) map (Test.apply))

    case Nil =>
      invalid(List(CsvError(1, "", "CSV file must contain at least one column")))
  }
}

val ans = read[Test](csv)
// ans: Seq[CsvValidated[Test]] =
//   Stream(
//     Valid(Test("x", Map("A" -> Some(1), "B" -> Some(2), "C" -> Some(3)))),
//     Valid(Test("y", Map("A" -> None,    "B" -> None,    "C" -> None))),
//     Valid(Test("z", Map("A" -> Some(3), "B" -> None,    "C" -> Some(1)))))
~~~

## Getting Started

Grab the code from Bintray by adding the following to your `build.sbt`:

~~~ scala
scalaVersion := "2.11.6"

resolvers += "Awesome Utilities" at "https://dl.bintray.com/davegurnell/maven"

libraryDependencies += "io.underscore" %% "csvside" % "0.7.0"
~~~

The import one of the following and proceed as above:

 - `csvside.list._` for list-formatted files;
 - `csvside.grid._` for grid-formatted files.

[license]: http://www.apache.org/licenses/LICENSE-2.0
