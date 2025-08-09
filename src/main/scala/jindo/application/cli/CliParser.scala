package jindo.application.cli

import jindo.application.command.CommandType
import scopt.OParser
import java.nio.file.{Path, Paths}

case class CliConfig(
    command: CommandType = CommandType.Install,
    projectRoot: Path = Paths.get("."),
    verbose: Boolean = false,
    help: Boolean = false
)

object CliParser {

  private val builder = OParser.builder[CliConfig]

  private val parser = {
    import builder._
    OParser.sequence(
      programName("jindo"),
      head("Jindo", "0.1.0"),
      help("help").text("Show this help message"),
      cmd("install")
        .action((_, c) => c.copy(command = CommandType.Install))
        .text("Install git hooks based on configuration")
        .children(
          opt[String]("project-root")
            .action((path, c) => c.copy(projectRoot = Paths.get(path)))
            .text("Project root directory (default: current directory)")
        ),
      cmd("run")
        .action((_, c) => c.copy(command = CommandType.Run))
        .text("Run configured hooks")
        .children(
          opt[String]("project-root")
            .action((path, c) => c.copy(projectRoot = Paths.get(path)))
            .text("Project root directory (default: current directory)")
        ),
      cmd("validate")
        .action((_, c) => c.copy(command = CommandType.Validate))
        .text("Validate configuration file")
        .children(
          opt[String]("project-root")
            .action((path, c) => c.copy(projectRoot = Paths.get(path)))
            .text("Project root directory (default: current directory)")
        ),
      cmd("list")
        .action((_, c) => c.copy(command = CommandType.ListHooks))
        .text("List configured hooks")
        .children(
          opt[String]("project-root")
            .action((path, c) => c.copy(projectRoot = Paths.get(path)))
            .text("Project root directory (default: current directory)")
        ),
      opt[Unit]('v', "verbose")
        .action((_, c) => c.copy(verbose = true))
        .text("Enable verbose output")
    )
  }

  def parse(args: Array[String]): Option[CliConfig] = {
    OParser.parse(parser, args, CliConfig())
  }

  private def showUsage(): Unit = {
    println(OParser.usage(parser))
  }

  def showHelp(): Unit = {
    println(
      """Jindo - JVM Integrated, No-Deps Operation
        |
        |A lightweight, JVM-based Git hooks manager.
        |
        |Examples:
        |  jindo install                    # Install hooks from .jindo.yaml
        |  jindo run                        # Run pre-commit hooks
        |  jindo validate                   # Validate configuration
        |  jindo list                       # List all configured hooks
        |  jindo install --project-root /path/to/project
        |
        |For more information, visit: https://github.com/jindo-io/jindo
        |""".stripMargin
    )
    println()
    showUsage()
  }
}
