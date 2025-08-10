package jindo.application.command

import jindo.domain.error.JindoError
import java.nio.file.{Files, Path, StandardOpenOption}

/** Command to initialize a new Jindo configuration */
class InitCommand(directory: Path, force: Boolean = false) extends Command {

  override def execute(): Either[JindoError, String] = {
    val configPath = directory.resolve(".jindo.yaml")
    val isOverwrite = Files.exists(configPath) && force

    for {
      _ <- checkIfConfigExists(configPath)
      _ <- writeConfigFile(configPath)
    } yield {
      val action = if (isOverwrite) "Overwritten" else "Created"
      s"$action .jindo.yaml in ${directory.toAbsolutePath}"
    }
  }

  private def checkIfConfigExists(
      configPath: Path
  ): Either[JindoError, Unit] = {
    if (Files.exists(configPath) && !force) {
      Left(
        JindoError.InvalidConfiguration(
          s"Configuration file already exists: ${configPath.toAbsolutePath}\n" +
            "Use --force to overwrite"
        )
      )
    } else {
      Right(())
    }
  }

  private def writeConfigFile(configPath: Path): Either[JindoError, Unit] = {
    try {
      val content = generateDefaultConfig()
      Files.write(
        configPath,
        content.getBytes("UTF-8"),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
      Right(())
    } catch {
      case e: Exception =>
        Left(
          JindoError.GitHookInstallationError(
            s"Failed to write configuration file: ${e.getMessage}"
          )
        )
    }
  }

  private def generateDefaultConfig(): String = {
    """# Jindo configuration file
      |# For more examples, see: https://github.com/jindo-io/jindo/tree/main/examples
      |
      |pre-commit:
      |  # Your loyal Jindo guard dog is on duty! 🐕
      |  - id: jindo-guard
      |    command: echo
      |    args: ["🐕 Woof! Your Jindo is sniffing through the code..."]
      |
      |  # Checkstyle for code quality
      |  - id: checkstyle
      |    main: com.puppycrawl.tools.checkstyle.Main
      |    dependencies:
      |      - com.puppycrawl.tools:checkstyle:10.12.1
      |    args: ["-c", "/google_checks.xml", "."]
      |
      |  # Victory tail wagging! 🎉
      |  - id: jindo-success
      |    command: echo
      |    args: ["🎉 Good boy! All checks passed - your Jindo is wagging its tail!"]
      |""".stripMargin
  }
}
