package jindo.application.cli

import picocli.CommandLine._
import java.util.concurrent.Callable
import jindo.application.command.{ValidateCommand => ValidateCommandImpl}

@Command(
  name = "validate",
  description = Array("Validate configuration file"),
  mixinStandardHelpOptions = true
)
class ValidateCommand extends Callable[Int] {

  @ParentCommand
  var parent: JindoCliApp = _

  override def call(): Int = {
    val directory = parent.directory
    val verbose = parent.verbose

    val command = new ValidateCommandImpl(directory)
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
