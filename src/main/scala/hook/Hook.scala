package hook

sealed trait Hook {
  def id: Option[String]
  def args: List[String]
}

case class JvmHook(
    id: Option[String],
    args: List[String],
    mainClass: String,
    repositories: Option[List[String]],
    dependencies: Option[List[String]]
) extends Hook

case class SystemHook(
    id: Option[String],
    args: List[String],
    command: String
) extends Hook
