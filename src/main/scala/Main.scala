import cli.Cli
import command.{Command, CommandFactory, CommandType}

object Main {
  def main(args: Array[String]): Unit = {
    Cli.parse(args) match {
      case Some(config) =>
        CommandType.fromString(config.command) match {
          case Some(cmdType) =>
            val cmd = CommandFactory.create(cmdType, config.projectRoot)
            cmd.execute()
          case None =>
            Cli.showUsage()
        }
      case None =>
        Cli.showUsage()
    }
  }
}
