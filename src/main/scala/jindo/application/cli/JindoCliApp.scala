package jindo.application.cli

import picocli.CommandLine
import picocli.CommandLine._
import java.nio.file.Path
import java.util.concurrent.Callable

@Command(
  name = "jindo",
  version = Array("0.1.0"),
  description = Array(
    "Jindo - JVM Integrated, No-Deps Operation",
    "",
    "A lightweight, JVM-based Git hooks manager."
  ),
  subcommands = Array(
    classOf[InitCommand],
    classOf[InstallCommand],
    classOf[RunCommand],
    classOf[ValidateCommand],
    classOf[ListCommand]
  ),
  mixinStandardHelpOptions = true
)
class JindoCliApp extends Callable[Int] {

  @Option(
    names = Array("-v", "--verbose"),
    description = Array("Enable verbose output")
  )
  var verbose: Boolean = false

  @Parameters(
    index = "0",
    paramLabel = "<path>",
    description = Array(
      "Directory containing .jindo.yaml configuration (default: current directory)"
    ),
    arity = "0..1"
  )
  var directory: Path = java.nio.file.Paths.get(".")

  override def call(): Int = {
    // Show ASCII art and help when no subcommand is provided
    showLogo()
    new CommandLine(this).usage(System.out)
    0
  }

  private def showLogo(): Unit = {
    println("""
      |     ██╗██╗███╗   ██╗██████╗  ██████╗ 
      |     ██║██║████╗  ██║██╔══██╗██╔═══██╗
      |     ██║██║██╔██╗ ██║██║  ██║██║   ██║
      |██   ██║██║██║╚██╗██║██║  ██║██║   ██║
      |╚█████╔╝██║██║ ╚████║██████╔╝╚██████╔╝
      | ╚════╝ ╚═╝╚═╝  ╚═══╝╚═════╝  ╚═════╝ 
      |                                      
      |🐕 Your loyal Git hooks guardian
      |""".stripMargin)
  }
}
