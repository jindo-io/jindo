package jindo.domain.error

sealed trait JindoError extends Exception {
  def message: String
  override def getMessage: String = message
}

object JindoError {
  case class ConfigNotFound(path: String) extends JindoError {
    val message: String = s"Configuration file not found: $path"
  }

  case class ConfigParsingError(details: String) extends JindoError {
    val message: String = s"Failed to parse configuration: $details"
  }

  case class InvalidConfiguration(details: String) extends JindoError {
    val message: String = s"Invalid configuration: $details"
  }

  case class DependencyResolutionError(dependency: String, cause: String)
      extends JindoError {
    val message: String = s"Failed to resolve dependency '$dependency': $cause"
  }

  case class HookExecutionError(hookId: String, cause: String)
      extends JindoError {
    val message: String = s"Hook '$hookId' execution failed: $cause"
  }

  case class GitHookInstallationError(cause: String) extends JindoError {
    val message: String = s"Failed to install git hooks: $cause"
  }

  case class UnsupportedCommand(command: String) extends JindoError {
    val message: String = s"Unsupported command: $command"
  }
}
