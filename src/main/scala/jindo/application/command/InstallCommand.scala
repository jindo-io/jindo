package jindo.application.command

import jindo.domain.config.{ConfigLoader, GitHookType}
import jindo.domain.error.JindoError
import jindo.infrastructure.git.GitHookInstaller
import java.nio.file.Path

class InstallCommand(projectRoot: Path) extends Command {

  private val configLoader = new ConfigLoader(projectRoot)
  private val gitHookInstaller = new GitHookInstaller(projectRoot)

  def execute(): Either[JindoError, String] = {
    for {
      config <- configLoader.loadConfig()
      _ <- validateGitRepository()
      installedHooks <- installHooks(config)
    } yield s"Git hooks installed successfully: ${installedHooks.mkString(", ")}"
  }

  private def validateGitRepository(): Either[JindoError, Unit] = {
    if (gitHookInstaller.isGitRepository) {
      Right(())
    } else {
      Left(JindoError.GitHookInstallationError("Not a git repository"))
    }
  }

  private def installHooks(
      config: jindo.domain.config.JindoConfig
  ): Either[JindoError, List[String]] = {
    val hookTypes =
      GitHookType.all.filter(hookType => config.getHooks(hookType).nonEmpty)

    val results = hookTypes.map { hookType =>
      gitHookInstaller.installHook(hookType) match {
        case Right(_)    => Right(hookType.name)
        case Left(error) => Left(error)
      }
    }

    // Collect all errors or return successful installations
    val (errors, successes) = results.partition(_.isLeft)

    if (errors.nonEmpty) {
      val errorMessages =
        errors.collect { case Left(error) => error.message }.mkString("; ")
      Left(JindoError.GitHookInstallationError(errorMessages))
    } else {
      Right(successes.collect { case Right(hookName) => hookName })
    }
  }
}
