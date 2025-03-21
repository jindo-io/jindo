import sbt.Keys._
import sbtassembly.MergeStrategy
import sbtassembly.PathList

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

lazy val commonSettings = Seq(
  organization := "io.vanslog",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
)

lazy val root = (project in file("."))
  .settings(
    name := "jindo",
    commonSettings,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml" % "0.14.2",
      "io.circe" %% "circe-generic" % "0.14.2",
      "io.get-coursier" %% "coursier" % "2.1.7",
      "com.github.scopt" %% "scopt" % "4.1.0",
    ),
    assembly / assemblyJarName := "jindo.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
