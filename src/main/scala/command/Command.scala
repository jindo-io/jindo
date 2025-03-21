package command

import java.nio.file.Path
import config.ConfigLoader
import hook.HookManager
import hook.HookExecutor

sealed trait Command {
  def execute(): Unit
}

case class InstallCommand(projectRoot: Path) extends Command {
  def execute(): Unit = {
    val configLoader = new ConfigLoader(projectRoot)
    val config = configLoader.loadConfig()

    val hookManager = new HookManager(projectRoot)
    hookManager.installHooks(config)
    println("Git hooks installed successfully")
  }
}

case class RunCommand(projectRoot: Path) extends Command {
  def execute(): Unit = {
    val configLoader = new ConfigLoader(projectRoot)
    val config = configLoader.loadConfig()

    val hookExecutor = new HookExecutor(projectRoot)
    hookExecutor.execute(config)
  }
}
