package jindo.application.command

import jindo.domain.config.{ConfigLoader, GitHookType}
import jindo.domain.error.JindoError
import jindo.domain.hook.{JvmHook, SystemHook}
import java.nio.file.Path

class ListCommand(projectRoot: Path) extends Command {

  private val configLoader = new ConfigLoader(projectRoot)

  def execute(): Either[JindoError, String] = {
    for {
      config <- configLoader.loadConfig()
    } yield formatHooksList(config)
  }

  private def formatHooksList(
      config: jindo.domain.config.JindoConfig
  ): String = {
    if (config.isEmpty) {
      "No hooks configured"
    } else {
      val sections = GitHookType.all.flatMap { hookType =>
        val hooks = config.getHooks(hookType)
        if (hooks.nonEmpty) {
          Some(formatHookSection(hookType, hooks))
        } else {
          None
        }
      }

      sections.mkString("\n\n")
    }
  }

  private def formatHookSection(
      hookType: GitHookType,
      hooks: List[jindo.domain.hook.Hook]
  ): String = {
    val header =
      s"${hookType.name} (${hooks.size} hook${if (hooks.size == 1) "" else "s"}):"
    val hooksList = hooks.zipWithIndex
      .map { case (hook, index) =>
        s"  ${index + 1}. ${formatHook(hook)}"
      }
      .mkString("\n")

    s"$header\n$hooksList"
  }

  private def formatHook(hook: jindo.domain.hook.Hook): String = {
    val id = hook.id.map(id => s"[$id] ").getOrElse("")

    hook match {
      case jvmHook: JvmHook =>
        val deps = jvmHook.dependencies.map(_.size).getOrElse(0)
        val depsText = if (deps > 0) s" ($deps dependencies)" else ""
        s"${id}JVM: ${jvmHook.mainClass}$depsText"

      case systemHook: SystemHook =>
        val argsText =
          if (systemHook.args.nonEmpty) s" ${systemHook.args.mkString(" ")}"
          else ""
        s"${id}System: ${systemHook.command}$argsText"
    }
  }
}
