package jindo

import jindo.application.cli.JindoCliApp
import picocli.CommandLine

object JindoApp {

  def main(args: Array[String]): Unit = {
    val exitCode = run(args)
    sys.exit(exitCode)
  }

  def run(args: Array[String]): Int = {
    val app = new JindoCliApp()
    val commandLine = new CommandLine(app)
    commandLine.execute(args: _*)
  }
}
