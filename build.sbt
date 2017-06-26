name               in ThisBuild := "csvside"
organization       in ThisBuild := "io.underscore"
version            in ThisBuild := "0.16.0-SNAPSHOT"
scalaVersion       in ThisBuild := "2.11.8"
crossScalaVersions in ThisBuild := Seq("2.11.8")

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

libraryDependencies ++= Seq(
  "com.bizo"        %% "mighty-csv" % "0.2",
  "org.typelevel"   %% "cats-core"  % "0.9.0",
  "com.chuusai"     %% "shapeless"  % "2.3.2",
  "com.davegurnell" %% "unindent"   % "1.0.0" % Test,
  "org.scalatest"   %% "scalatest"  % "2.2.4" % Test
)

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
