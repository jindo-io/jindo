package command

import java.nio.file.Path

object CommandFactory {
  def create(cmdType: CommandType, projectRoot: Path): Command = cmdType match {
    case CommandType.Install => InstallCommand(projectRoot)
    case CommandType.Run     => RunCommand(projectRoot)
  }
}
