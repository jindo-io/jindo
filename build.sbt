import sbt.Keys._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

lazy val commonSettings = Seq(
  organization := "io.vanslog",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Wunused:imports",
    "-Xlint:unused"
  )
)

lazy val root = (project in file("."))
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    name := "jindo",
    commonSettings,

    // Main class for the application
    Compile / mainClass := Some("jindo.JindoApp"),

    // Runtime dependencies
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-yaml" % "0.14.2",
      "io.circe" %% "circe-generic" % "0.14.2",
      "io.circe" %% "circe-generic-extras" % "0.14.2",
      "io.get-coursier" %% "coursier" % "2.1.7",
      "com.github.scopt" %% "scopt" % "4.1.0"
    ),

    // Test dependencies
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test
    ),

    // GraalVM native image settings
    graalVMNativeImageOptions ++= Seq(
      "--enable-url-protocols=https",
      "--enable-url-protocols=http",
      "--initialize-at-build-time",
      "--no-fallback",
      "--install-exit-handlers"
    ),

    // Test settings
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    Test / parallelExecution := false,

    // Assembly settings for fat JAR
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    },

    // Scalafmt settings
    scalafmtOnCompile := true
  )
