package jindo.application.command

import jindo.domain.error.JindoError
import java.nio.file.Path

/** Base trait for all Jindo commands */
trait Command {

  /** Execute the command and return either success or error */
  def execute(): Either[JindoError, String]
}

/** Command types supported by Jindo */
sealed trait CommandType {
  def name: String
}

object CommandType {
  case object Install extends CommandType { val name = "install" }
  case object Run extends CommandType { val name = "run" }
  case object Validate extends CommandType { val name = "validate" }
  case object ListHooks extends CommandType { val name = "list" }

  val all: List[CommandType] = List(Install, Run, Validate, ListHooks)
}

/** Factory for creating commands */
object CommandFactory {
  def create(commandType: CommandType, projectRoot: Path): Command =
    commandType match {
      case CommandType.Install   => new InstallCommand(projectRoot)
      case CommandType.Run       => new RunCommand(projectRoot)
      case CommandType.Validate  => new ValidateCommand(projectRoot)
      case CommandType.ListHooks => new ListCommand(projectRoot)
    }
}
