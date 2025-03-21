package hook

import coursier._
import coursier.core.Authentication
import coursier.cache.FileCache
import coursier.parse.DependencyParser

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import config.Config

/** Executes git hooks with dependency resolution */
class HookExecutor(projectRoot: Path) {
  private val cache = FileCache()

  /** Execute a hook
    *
    * @param hook
    *   the hook to execute
    * @return
    *   true if the hook execution was successful
    */
  def execute(config: Config): Unit = {
    config.preCommit.foreach { hooks =>
      hooks.foreach { hook =>
        val success = executeHook(hook)
        if (!success) {
          System.exit(1)
        }
      }
    }
  }

  private def executeHook(hook: Hook): Boolean = hook match {
    case jvm: JvmHook    => executeJvmHook(jvm)
    case sys: SystemHook => executeSystemHook(sys)
  }

  private def executeJvmHook(hook: JvmHook): Boolean = {
    val repositories = hook.repositories
      .map(_.map(MavenRepository(_)))
      .getOrElse(Seq.empty)

    val dependencies = hook.dependencies
      .map(
        _.flatMap(dep =>
          DependencyParser.dependency(dep, defaultScalaVersion = "2.13").toSeq
        )
      )
      .getOrElse(Seq.empty)

    val fetch = Fetch()
      .addDependencies(dependencies: _*)
      .addRepositories(repositories: _*)

    val files = Await.result(
      fetch.future(),
      1.minute
    )

    val classpath = files.map(_.getAbsolutePath).mkString(File.pathSeparator)
    val process = new ProcessBuilder(
      (Seq("java", "-cp", classpath, hook.mainClass) ++ hook.args): _*
    )
      .directory(projectRoot.toFile)
      .inheritIO()
      .start()

    process.waitFor() == 0
  }

  private def executeSystemHook(hook: SystemHook): Boolean = {
    val process = new ProcessBuilder((hook.command +: hook.args): _*)
      .directory(projectRoot.toFile)
      .inheritIO()
      .start()

    process.waitFor() == 0
  }
}
