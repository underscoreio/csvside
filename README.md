# CSVSide

CSV readers and combinators for Scala. Made with Cats.

Copyright 2015 Richard Dallaway and Dave Gurnell. Licensed [Apache 2][license].

## Synopsis

CSVSide lets you read list-formatted and grid-formatted CSV files as follows.

### List-Formatted Files

Here's an example that reads directly from a `String`.
You can also read from a `java.io.File` or a `java.io.Reader`:

~~~ scala
import csvside.list._

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
//     Invalid(List("Int: Must be a whole number")),
//     Invalid(List(
//       "Int: Must be a whole number",
//       "Bool: Must be a yes/no value or blank"
//     ))
//   )
~~~

### Grid-Formatted Files

You can read grid-oriented files as follows:

~~~ scala
import csvside.grid._

// We want to parse the cells in this CSV as Option[Boolean]:
val csv = """
  |,A,B,C
  |1,true,false,true
  |2,false,,false
  |3,true,false,true
""".trim.stripMargin

// We simply call read:
val ans = read[Option[Boolean]](csv)
// ans: Validated[Seq[String], Map[(String, String), Option[Boolean]]] =
//   Map(
//     ("A", "1") -> Some(true),
//     ("A", "2") -> Some(false),
//     ("A", "3") -> Some(true),
//     ("B", "1") -> Some(false),
//     ("B", "2") -> None,
//     ("B", "3") -> Some(false),
//     ("C", "1") -> Some(true),
//     ("C", "2") -> Some(false),
//     ("C", "3") -> Some(true)
//   )
~~~

## Getting Started

Grab the code from Bintray by adding the following to your `build.sbt`:

~~~ scala
scalaVersion := "2.11.6"

resolvers += "Awesome Utilities" at "https://dl.bintray.com/davegurnell/maven"

libraryDependencies += "io.underscore" %% "csvside" % "0.1"
~~~

The import one of the following and proceed as above:

 - `csvside.list._` for list-formatted files;
 - `csvside.grid._` for grid-formatted files.

[license]: http://www.apache.org/licenses/LICENSE-2.0
