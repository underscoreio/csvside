# CSVSide

CSV readers and combinators for Scala. Made with Cats.

Copyright 2014 Richard Dallaway and Dave Gurnell of Underscore.

## Synopsis

Read a CSV file like this:

~~~ csv
Column 1,Column 2,Column 3, Column 4
abc,123,true,
~~~

using Scala code like this:

~~~ scala
import csvside._
import csvside.list._
import cats.data.Validated
import cats.syntax.apply._

case class Test(a: String, b: Int, c: Boolean, d: Option[Double])

val testFormat = (
  "Column 1".as[String] |@|
  "Column 2".as[Int] |@|
  "Column 3".as[Boolean] |@|
  "Column 4".as[Option[Double]]
) map (Test.apply)

val data: Seq[Validated[List[String], Test]] =
  load[CsvSpec]("sample.csv")
~~~

Each line in the CSV becomes an item in the list (except the first line, which contains headers). Invalid lines become instances of `Validated.Invalid` containing relevant error messages.