# CSVSide

CSV readers and combinators for Scala. Made with Cats.

Copyright 2015 Richard Dallaway and Dave Gurnell. Licensed [Apache 2][license].

[![Build Status](https://travis-ci.org/underscoreio/csvside.svg?branch=develop)](https://travis-ci.org/davegurnell/csvside)

## Getting Started

Grab the code by adding the following to your `build.sbt`:

~~~ scala
libraryDependencies += "io.underscore" %% "csvside" % "<<VERSION>>"
~~~

### Fixed Row Reader

Here's an example that reads directly from a `String`.
You can also read from a `java.io.File` or a `java.io.Reader`:

~~~ scala
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

// We import Csvside...
import csvside._

// We define a RowFormat...
import cats.syntax.cartesian._
implicit val testFormat: RowFormat[Test] = (
  "Str".csv[String] |@|
  "Int".csv[Int] |@|
  "Bool".csv[Option[Boolean]]
).imap(Test.apply)(unlift(Test.unapply))

// We read the data...
val ans = Csv.fromString[Test](csv).toList
// ans: List[csvside.CsvValidated[Test]] = List(
//   Valid(Test(abc,123,Some(true))),
//   Valid(Test(a b,321,Some(false))),
//   Invalid(List(CsvError(4,Int,Must be a whole number))),
//   Invalid(List(CsvError(5,Int,Must be a whole number),
//                CsvError(5,Bool,Must be a yes/no value or blank))))

// And we write it back to CSV...
import cats.data.Validated
val validOnly = ans collect { case Validated.Valid(test) => test }
val finalCsv = Csv.toString(validOnly)
// finalCsv: String =
// ""Str","Int","Bool"
// "abc","123","true"
// "a b","321","false"
// "
~~~

### Row Reader Depends on Header Row

If the format of the rows depends on the values in the header row,
we can use a `ListReader[A]` to generate a `ColumnReader[A]` on the fly:

~~~ scala
// We want to parse the cells in this CSV...
val csv = s"""
  |Row,Col1,Col2,Col3
  |x,1,2,3
  |y,,,
  |z,3,,1
""".trim.stripMargin

// To a sequence of this data structure...
case class Test(key: String, values: Map[CsvPath, Option[Int]])

// We do this by creating a `ListReader` that
// parses the column headings and creates a `ColumnReader`
// to read the rest of the file.

// We import from Csvside...
import csvside._

// We define a ListReader...
import cats.data.Validated.{valid, invalid}
import cats.syntax.cartesian._
import cats.syntax.validated._
implicit val testReader: ListReader[Test] =
  ListReader[Test] {
    case head :: tail =>
      (head.read[String] |@| tail.readMap[Option[Int]]).map(Test.apply).valid

    case Nil =>
      invalid(List(CsvError(1, "", "File must contain at least one column")))
  }

// And we read the data...
val ans = Csv.fromString[Test](csv).toList
// ans: List[CsvValidated[Test]] =
//   List(
//     Valid(Test("x", Map("Col1" -> Some(1), "Col2" -> Some(2), "Col3" -> Some(3)))),
//     Valid(Test("y", Map("Col1" -> None,    "Col2" -> None,    "Col3" -> None))),
//     Valid(Test("z", Map("Col1" -> Some(3), "Col2" -> None,    "Col3" -> Some(1)))))
~~~

[license]: http://www.apache.org/licenses/LICENSE-2.0
