package jindo.application.cli

import picocli.CommandLine._
import java.util.concurrent.Callable
import jindo.application.command.{InitCommand => InitCommandImpl}

@Command(
  name = "init",
  description = Array("Create a sample .jindo.yaml configuration file"),
  mixinStandardHelpOptions = true
)
class InitCommand extends Callable[Int] {

  @Option(
    names = Array("-f", "--force"),
    description = Array("Overwrite existing configuration file")
  )
  var force: Boolean = false

  @ParentCommand
  var parent: JindoCliApp = _

  override def call(): Int = {
    val directory = parent.directory
    val verbose = parent.verbose

    val command = new InitCommandImpl(directory, force)
    command.execute() match {
      case Right(message) =>
        println(message)
        0
      case Left(error) =>
        println(s"Error: ${error.message}")
        if (verbose) error.printStackTrace()
        1
    }
  }
}
