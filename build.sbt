ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .settings(
    name := "TestTask"
  )

libraryDependencies ++=
  Seq("org.typelevel" %% "cats-effect" % "3.7.1",
    "co.fs2" %% "fs2-io" % "3.14.0")
