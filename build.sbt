name := "he"

version := "0.1"

scalaVersion := "2.11.8"

Defaults.itSettings

lazy val `it-config-sbt-project` = project.in(file(".")).configs(IntegrationTest)

libraryDependencies ++= Seq(
  "com.github.finagle" %% "finch-core" % "0.11.0-M2" changing(),
  "com.github.finagle" %% "finch-argonaut" % "0.11.0-M2" changing(),
  "io.argonaut" %% "argonaut" % "6.1",
  "com.github.finagle" %% "finch-test" % "0.11.0-M2" % "test,it" changing(),
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test,it",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test,it"
)
