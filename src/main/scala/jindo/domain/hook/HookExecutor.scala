package jindo.domain.hook

import jindo.domain.error.JindoError
import jindo.infrastructure.coursier.DependencyResolver
import java.nio.file.Path
import scala.util.{Try, Success, Failure}
import scala.sys.process._

class HookExecutor(projectRoot: Path) {

  private val dependencyResolver = new DependencyResolver()

  /** Execute a list of hooks in sequence */
  def executeHooks(hooks: List[Hook]): Either[JindoError, List[String]] = {
    val results = hooks.map(executeHook)

    // Check if any hook failed
    val (failures, successes) = results.partition(_.isLeft)

    if (failures.nonEmpty) {
      val firstFailure = failures.head.left.toOption.get
      Left(firstFailure)
    } else {
      Right(successes.collect { case Right(result) => result })
    }
  }

  /** Execute a single hook */
  def executeHook(hook: Hook): Either[JindoError, String] = {
    hook match {
      case jvmHook: JvmHook       => executeJvmHook(jvmHook)
      case systemHook: SystemHook => executeSystemHook(systemHook)
    }
  }

  private def executeJvmHook(hook: JvmHook): Either[JindoError, String] = {
    for {
      classpath <- resolveDependencies(hook)
      result <- runJvmApplication(hook, classpath)
    } yield result
  }

  private def executeSystemHook(
      hook: SystemHook
  ): Either[JindoError, String] = {
    runSystemCommand(hook)
  }

  private def resolveDependencies(hook: JvmHook): Either[JindoError, String] = {
    hook.dependencies match {
      case Some(deps) if deps.nonEmpty =>
        dependencyResolver.resolve(deps, hook.getRepositories) match {
          case Right(classpath) => Right(classpath)
          case Left(error)      => Left(error)
        }
      case _ =>
        Right("") // No dependencies
    }
  }

  private def runJvmApplication(
      hook: JvmHook,
      classpath: String
  ): Either[JindoError, String] = {
    val command = buildJvmCommand(hook, classpath)

    Try {
      val process = Process(command, projectRoot.toFile)
      val exitCode = process.!

      if (exitCode == 0) {
        s"${hook.identifier} completed successfully"
      } else {
        throw new RuntimeException(s"Process exited with code $exitCode")
      }
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        Left(
          JindoError.HookExecutionError(hook.identifier, exception.getMessage)
        )
    }
  }

  private def runSystemCommand(hook: SystemHook): Either[JindoError, String] = {
    val workingDir = hook.workingDirectory
      .map(java.nio.file.Paths.get(_).toFile)
      .getOrElse(projectRoot.toFile)

    Try {
      val process = Process(hook.fullCommand, workingDir)
      val exitCode = process.!

      if (exitCode == 0) {
        s"${hook.identifier} completed successfully"
      } else {
        throw new RuntimeException(s"Process exited with code $exitCode")
      }
    } match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        Left(
          JindoError.HookExecutionError(hook.identifier, exception.getMessage)
        )
    }
  }

  private def buildJvmCommand(
      hook: JvmHook,
      classpath: String
  ): List[String] = {
    val baseCommand = List("java")
    val classpathArg =
      if (classpath.nonEmpty) List("-cp", classpath) else List.empty
    val mainClass = List(hook.mainClass)
    val args = hook.args

    baseCommand ++ classpathArg ++ mainClass ++ args
  }
}
