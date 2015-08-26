name := "csvside"

organization := "io.underscore"

version := "0.8.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

// Cats:

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.spire-math" %% "cats-core" % "0.1.0-SNAPSHOT",
  "org.spire-math" %% "cats-std"  % "0.1.0-SNAPSHOT"
)

// MightyCSV:

libraryDependencies ++= Seq(
  "com.bizo" %% "mighty-csv" % "0.2"
)

// Scalatest:

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// Unindent:

resolvers += "Awesome Utilities" at "https://dl.bintray.com/davegurnell/maven"

libraryDependencies ++= Seq(
  "com.davegurnell" %% "unindent" % "1.0.0"
)

// Bintray:

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

bintrayPackageLabels in bintray := Seq("scala", "csv", "utility")

bintrayRepository in bintray := "maven"
