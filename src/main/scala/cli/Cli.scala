package cli

import scopt.OParser
import java.nio.file.{Path, Paths}

case class CliConfig(
    command: String = "",
    projectRoot: Path = Paths.get(scala.util.Properties.userDir)
)

object Cli {
  private val builder = OParser.builder[CliConfig]
  private val parser = {
    import builder._
    OParser.sequence(
      programName("jindo"),
      head("jindo", "0.1.0"),
      cmd("install")
        .action((_, c) => c.copy(command = "install"))
        .text("Install git hooks based on .jindo.yaml configuration"),
      cmd("run")
        .action((_, c) => c.copy(command = "run"))
        .text("Run git hooks")
    )
  }

  def parse(args: Array[String]): Option[CliConfig] = {
    OParser.parse(parser, args, CliConfig())
  }

  def showUsage(): Unit = {
    System.err.println(OParser.usage(parser))
  }
}
