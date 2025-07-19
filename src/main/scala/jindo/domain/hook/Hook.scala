package jindo.domain.hook

/** Base trait for all hook types */
sealed trait Hook {
  def id: Option[String]
  def args: List[String]

  /** Get a human-readable identifier for this hook */
  def identifier: String = id.getOrElse(this.getClass.getSimpleName)

  /** Dependencies required by this hook */
  def dependencies: Option[List[String]]

  /** Repositories to search for dependencies */
  def repositories: Option[List[String]]
}

/** Hook that executes a JVM application with dependencies */
case class JvmHook(
    id: Option[String] = None,
    args: List[String] = List.empty,
    mainClass: String,
    dependencies: Option[List[String]] = None,
    repositories: Option[List[String]] = None
) extends Hook {

  /** Validate that the main class is properly formatted */
  def isValid: Boolean = mainClass.nonEmpty && mainClass.contains(".")

  /** Get default repositories if none specified */
  def getRepositories: List[String] = repositories.getOrElse(
    List("https://repo1.maven.org/maven2/")
  )
}

/** Hook that executes a system command */
case class SystemHook(
    id: Option[String] = None,
    args: List[String] = List.empty,
    command: String,
    workingDirectory: Option[String] = None
) extends Hook {
  val dependencies: Option[List[String]] = None
  val repositories: Option[List[String]] = None

  /** Validate that the command is not empty */
  def isValid: Boolean = command.trim.nonEmpty

  /** Get full command with arguments */
  def fullCommand: List[String] = command :: args
}

object Hook {

  /** Create a JVM hook with sensible defaults */
  def jvm(
      mainClass: String,
      dependencies: List[String] = List.empty,
      args: List[String] = List.empty,
      id: Option[String] = None
  ): JvmHook = {
    JvmHook(
      id = id,
      args = args,
      mainClass = mainClass,
      dependencies = if (dependencies.nonEmpty) Some(dependencies) else None
    )
  }

  /** Create a system hook with sensible defaults */
  def system(
      command: String,
      args: List[String] = List.empty,
      id: Option[String] = None,
      workingDirectory: Option[String] = None
  ): SystemHook = {
    SystemHook(
      id = id,
      args = args,
      command = command,
      workingDirectory = workingDirectory
    )
  }
}
