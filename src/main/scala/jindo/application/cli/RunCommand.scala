package jindo.application.cli

import picocli.CommandLine._
import java.util.concurrent.Callable
import jindo.application.command.{RunCommand => RunCommandImpl}

@Command(
  name = "run",
  description = Array("Run configured hooks"),
  mixinStandardHelpOptions = true
)
class RunCommand extends Callable[Int] {

  @ParentCommand
  var parent: JindoCliApp = _

  override def call(): Int = {
    val directory = parent.directory
    val verbose = parent.verbose

    val command = new RunCommandImpl(directory)
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
