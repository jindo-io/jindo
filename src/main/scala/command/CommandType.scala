package command

sealed trait CommandType

object CommandType {
  case object Install extends CommandType
  case object Run extends CommandType

  def fromString(cmd: String): Option[CommandType] = cmd match {
    case "install" => Some(Install)
    case "run"     => Some(Run)
    case _         => None
  }
}
