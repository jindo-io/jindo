package jindo.application.cli

import jindo.application.command.CommandType
import scopt.OParser
import java.nio.file.{Path, Paths}

case class CliConfig(
    command: CommandType = CommandType.Init,
    directory: Option[Path] = None,
    verbose: Boolean = false,
    force: Boolean = false
)

object CliParser {

  private val builder = OParser.builder[CliConfig]

  private val commonOptions = {
    import builder._
    Seq(
      arg[String]("<path>")
        .optional()
        .action((path, c) => c.copy(directory = Some(Paths.get(path))))
        .text(
          "Directory containing .jindo.yaml configuration (default: current directory)"
        ),
      opt[Unit]('v', "verbose")
        .action((_, c) => c.copy(verbose = true))
        .text("Enable verbose output")
    )
  }

  private val initOptions = {
    import builder._
    commonOptions ++ Seq(
      opt[Unit]('f', "force")
        .action((_, c) => c.copy(force = true))
        .text("Overwrite existing configuration file")
    )
  }

  private val parser = {
    import builder._
    OParser.sequence(
      programName("jindo"),
      head("Jindo", "0.1.0"),
      help("help").text("Show this help message"),
      cmd("init")
        .action((_, c) => c.copy(command = CommandType.Init))
        .text("Create a sample .jindo.yaml configuration file")
        .children(initOptions: _*),
      cmd("install")
        .action((_, c) => c.copy(command = CommandType.Install))
        .text("Install git hooks based on configuration")
        .children(commonOptions: _*),
      cmd("run")
        .action((_, c) => c.copy(command = CommandType.Run))
        .text("Run configured hooks")
        .children(commonOptions: _*),
      cmd("validate")
        .action((_, c) => c.copy(command = CommandType.Validate))
        .text("Validate configuration file")
        .children(commonOptions: _*),
      cmd("list")
        .action((_, c) => c.copy(command = CommandType.ListHooks))
        .text("List configured hooks")
        .children(commonOptions: _*)
    )
  }

  def parse(args: Array[String]): Option[CliConfig] = {
    OParser.parse(parser, args, CliConfig())
  }
}
