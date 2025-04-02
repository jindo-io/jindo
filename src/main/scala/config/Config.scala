package config

import hook.Hook

/** Configuration for git hooks */
case class Config(
    preCommit: Option[List[Hook]] = None
)
