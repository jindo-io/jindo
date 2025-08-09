package jindo.application.command

import jindo.domain.config.ConfigLoader
import jindo.domain.error.JindoError
import java.nio.file.Path

class ValidateCommand(projectRoot: Path) extends Command {

  private val configLoader = new ConfigLoader(projectRoot)

  def execute(): Either[JindoError, String] = {
    for {
      config <- configLoader.loadConfig()
      validationResult <- validateConfig(config)
    } yield validationResult
  }

  private def validateConfig(
      config: jindo.domain.config.JindoConfig
  ): Either[JindoError, String] = {
    val issues = scala.collection.mutable.ListBuffer[String]()

    // Check if the configuration is empty
    if (config.isEmpty) {
      issues += "Configuration is empty - no hooks defined"
    }

    // Validate all hooks
    val allHooks = config.preCommit

    allHooks.zipWithIndex.foreach { case (hook, index) =>
      hook match {
        case jvmHook: jindo.domain.hook.JvmHook =>
          if (!jvmHook.isValid) {
            issues += s"Hook ${index + 1}: Invalid JVM hook - main class '${jvmHook.mainClass}' is not properly formatted"
          }
          if (jvmHook.dependencies.exists(_.exists(_.trim.isEmpty))) {
            issues += s"Hook ${index + 1}: Empty dependency found"
          }

        case systemHook: jindo.domain.hook.SystemHook =>
          if (!systemHook.isValid) {
            issues += s"Hook ${index + 1}: Invalid system hook - command cannot be empty"
          }
      }
    }

    // Check for duplicate hook IDs
    val hookIds = allHooks.flatMap(_.id)
    val duplicateIds = hookIds.groupBy(identity).collect {
      case (id, occurrences) if occurrences.length > 1 => id
    }
    if (duplicateIds.nonEmpty) {
      issues += s"Duplicate hook IDs found: ${duplicateIds.mkString(", ")}"
    }

    if (issues.nonEmpty) {
      Left(
        JindoError.InvalidConfiguration(
          s"Configuration validation failed:\n${issues.map("  - " + _).mkString("\n")}"
        )
      )
    } else {
      val hookCount = allHooks.size
      val hookTypes = if (config.preCommit.nonEmpty) {
        s"${config.preCommit.size} pre-commit"
      } else {
        "no hooks"
      }

      Right(s"Configuration is valid: $hookCount total hooks ($hookTypes)")
    }
  }
}
