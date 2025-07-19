package jindo.domain.config

import jindo.domain.hook.Hook

/** Configuration for Jindo git hooks management */
case class JindoConfig(
    preCommit: List[Hook] = List.empty
) {

  /** Get all hooks for a specific git hook type */
  def getHooks(hookType: GitHookType): List[Hook] = hookType match {
    case GitHookType.PreCommit => preCommit
  }

  /** Check if configuration is empty */
  def isEmpty: Boolean = preCommit.isEmpty

  /** Get all unique dependencies across all hooks */
  def getAllDependencies: Set[String] = {
    preCommit.flatMap(_.dependencies.getOrElse(List.empty)).toSet
  }
}

sealed trait GitHookType {
  def name: String
}

object GitHookType {
  case object PreCommit extends GitHookType { val name = "pre-commit" }

  def fromString(str: String): Option[GitHookType] = str.toLowerCase match {
    case "pre-commit" => Some(PreCommit)
    case _            => None
  }

  val all: List[GitHookType] = List(PreCommit)
}
