package jindo.infrastructure.git

import jindo.domain.config.GitHookType
import jindo.domain.error.JindoError
import java.nio.file.{Files, Path, StandardOpenOption}
import java.nio.charset.StandardCharsets
import scala.util.{Try, Success, Failure}

class GitHookInstaller(projectRoot: Path) {

  private val gitHooksDir = projectRoot.resolve(".git/hooks")

  /** Check if the current directory is a git repository */
  def isGitRepository: Boolean = {
    Files.exists(projectRoot.resolve(".git")) || Files.exists(
      projectRoot.resolve(".git/config")
    )
  }

  /** Install a git hook for the specified type */
  def installHook(hookType: GitHookType): Either[JindoError, Unit] = {
    for {
      _ <- validateGitRepository()
      _ <- ensureHooksDirectory()
      _ <- writeHookScript(hookType)
      _ <- makeHookExecutable(hookType)
    } yield ()
  }

  private def validateGitRepository(): Either[JindoError, Unit] = {
    if (isGitRepository) {
      Right(())
    } else {
      Left(JindoError.GitHookInstallationError("Not a git repository"))
    }
  }

  private def ensureHooksDirectory(): Either[JindoError, Unit] = {
    Try {
      Files.createDirectories(gitHooksDir)
    } match {
      case Success(_) => Right(())
      case Failure(exception) =>
        Left(
          JindoError.GitHookInstallationError(
            s"Failed to create hooks directory: ${exception.getMessage}"
          )
        )
    }
  }

  private def writeHookScript(
      hookType: GitHookType
  ): Either[JindoError, Unit] = {
    val hookFile = getHookPath(hookType)
    val hookScript = generateHookScript(hookType)

    Try {
      Files.write(
        hookFile,
        hookScript.getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
    } match {
      case Success(_) => Right(())
      case Failure(exception) =>
        Left(
          JindoError.GitHookInstallationError(
            s"Failed to write hook script: ${exception.getMessage}"
          )
        )
    }
  }

  private def makeHookExecutable(
      hookType: GitHookType
  ): Either[JindoError, Unit] = {
    val hookFile = getHookPath(hookType)

    Try {
      val perms = Files.getPosixFilePermissions(hookFile)
      import java.nio.file.attribute.PosixFilePermission._
      perms.addAll(
        java.util.Set.of(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE)
      )
      Files.setPosixFilePermissions(hookFile, perms)
    } match {
      case Success(_) => Right(())
      case Failure(exception) =>
        Left(
          JindoError.GitHookInstallationError(
            s"Failed to make hook executable: ${exception.getMessage}"
          )
        )
    }
  }

  private def getHookPath(hookType: GitHookType): Path = {
    gitHooksDir.resolve(hookType.name)
  }

  private def generateHookScript(hookType: GitHookType): String = {
    val jindoBinary = findJindoBinary()

    s"""#!/bin/sh
       |# Jindo git hook for ${hookType.name}
       |# Generated automatically - do not edit manually
       |
       |cd "$$(git rev-parse --show-toplevel)"
       |exec $jindoBinary run
       |""".stripMargin
  }

  private def findJindoBinary(): String = {
    // Try to find jindo binary in various locations
    val candidates = List(
      "jindo", // Assume it's in PATH
      "./target/graalvm-native-image/jindo", // Local build
      "/usr/local/bin/jindo", // Homebrew installation
      "/opt/homebrew/bin/jindo" // Apple Silicon Homebrew
    )

    candidates
      .find { path =>
        Try(Runtime.getRuntime.exec(Array("which", path)))
          .map(_.waitFor() == 0)
          .getOrElse(false) ||
        Files.exists(java.nio.file.Paths.get(path))
      }
      .getOrElse("jindo") // Fallback to assuming it's in PATH
  }
}
