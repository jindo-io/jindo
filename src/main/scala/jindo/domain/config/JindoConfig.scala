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

}

sealed trait GitHookType {
  def name: String
}

object GitHookType {
  case object PreCommit extends GitHookType { val name = "pre-commit" }

  val all: List[GitHookType] = List(PreCommit)
}
