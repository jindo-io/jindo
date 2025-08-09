package jindo

import jindo.application.cli.CliParser
import jindo.application.command.CommandFactory
import jindo.domain.error.JindoError

object JindoApp {

  def main(args: Array[String]): Unit = {
    val exitCode = run(args)
    sys.exit(exitCode)
  }

  def run(args: Array[String]): Int = {
    CliParser.parse(args) match {
      case Some(config) =>
        executeCommand(config)
      case None =>
        // scopt already printed error message
        1
    }
  }

  private def executeCommand(
      config: jindo.application.cli.CliConfig
  ): Int = {
    val projectRoot = config.directory.getOrElse(java.nio.file.Paths.get("."))
    val command = CommandFactory.create(config.command, projectRoot)

    command.execute() match {
      case Right(message) =>
        println(message)
        0
      case Left(error) =>
        handleError(error, config.verbose)
        1
    }
  }

  private def handleError(error: JindoError, verbose: Boolean): Unit = {
    error match {
      case JindoError.ConfigNotFound(path) =>
        println(s"Error: Configuration file not found at $path")
        println(
          "Run 'jindo init' to create a sample configuration, or create .jindo.yaml manually."
        )

      case JindoError.ConfigParsingError(details) =>
        println(s"Error: Failed to parse configuration file")
        if (verbose) println(s"Details: $details")

      case JindoError.InvalidConfiguration(details) =>
        println(s"Error: Invalid configuration")
        println(details)

      case JindoError.DependencyResolutionError(dependency, cause) =>
        println(s"Error: Failed to resolve dependency '$dependency'")
        if (verbose) println(s"Cause: $cause")

      case JindoError.HookExecutionError(hookId, cause) =>
        println(s"Error: Hook '$hookId' execution failed")
        if (verbose) println(s"Cause: $cause")

      case JindoError.GitHookInstallationError(cause) =>
        println(s"Error: Git hook installation failed")
        if (verbose) println(s"Cause: $cause")

      case JindoError.UnsupportedCommand(command) =>
        println(s"Error: Unsupported command '$command'")
        println("Supported commands: install, run, validate, list")

      case _ =>
        println(s"Error: ${error.message}")
        if (verbose) {
          println("Stack trace:")
          error.printStackTrace()
        }
    }
  }
}
