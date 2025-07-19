package jindo.application.command

import jindo.domain.config.ConfigLoader
import jindo.domain.error.JindoError
import jindo.domain.hook.HookExecutor
import java.nio.file.Path

class RunCommand(projectRoot: Path) extends Command {

  private val configLoader = new ConfigLoader(projectRoot)
  private val hookExecutor = new HookExecutor(projectRoot)

  def execute(): Either[JindoError, String] = {
    for {
      config <- configLoader.loadConfig()
      _ <- validateConfiguration(config)
      results <- executeHooks(config)
    } yield formatResults(results)
  }

  private def validateConfiguration(
      config: jindo.domain.config.JindoConfig
  ): Either[JindoError, Unit] = {
    if (config.isEmpty) {
      Left(JindoError.InvalidConfiguration("No hooks configured"))
    } else {
      Right(())
    }
  }

  private def executeHooks(
      config: jindo.domain.config.JindoConfig
  ): Either[JindoError, List[String]] = {
    // For now, execute pre-commit hooks. In the future, this could be parameterized
    val hooks = config.preCommit

    if (hooks.isEmpty) {
      Right(List("No pre-commit hooks to execute"))
    } else {
      hookExecutor.executeHooks(hooks)
    }
  }

  private def formatResults(results: List[String]): String = {
    if (results.size == 1) {
      results.head
    } else {
      s"Executed ${results.size} hooks successfully"
    }
  }
}
