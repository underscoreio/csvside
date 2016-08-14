name         := "csvside"
organization := "io.underscore"
version      := "0.15.0"
scalaVersion := "2.11.8"

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "com.bizo"        %% "mighty-csv" % "0.2",
  "org.typelevel"   %% "cats"       % "0.4.1",
  "com.davegurnell" %% "unindent"   % "1.0.0" % "test",
  "org.scalatest"   %% "scalatest"  % "2.2.4" % "test"
)

sonatypeProfileName := "io.underscore"

pomExtra in Global := {
  <url>https://github.com/underscoreio/csvside</url>
  <scm>
    <connection>scm:git:github.com/underscoreio/csvside</connection>
    <developerConnection>scm:git:git@github.com:underscoreio/csvside</developerConnection>
    <url>github.com/underscoreio/csvside</url>
  </scm>
  <developers>
    <developer>
      <id>d6y</id>
      <name>Richard Dallaway</name>
      <url>http://twitter.com/d6y</url>
    </developer>
    <developer>
      <id>davegurnell</id>
      <name>Dave Gurnell</name>
      <url>http://twitter.com/davegurnell</url>
    </developer>
  </developers>
}
