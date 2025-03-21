package hook

import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.nio.file.attribute.PosixFilePermission
import scala.jdk.CollectionConverters._
import config.Config

/** Manages git hooks in the project */
class HookManager(projectRoot: Path) {
  private val gitHooksDir = projectRoot.resolve(".git/hooks")
  private val hookTemplate = """#!/bin/sh
                               |exec jindo run
                               |""".stripMargin

  /** Install git hooks in the project
    *
    * @param config
    *   the configuration to use
    */
  def installHooks(config: Config): Unit = {
    if (!Files.exists(gitHooksDir)) {
      throw new IllegalStateException("Not a git repository")
    }
    config.preCommit.foreach(_ => installHook("pre-commit"))
  }

  private def installHook(hookName: String): Unit = {
    val hookFile = gitHooksDir.resolve(hookName)
    Files.write(
      hookFile,
      hookTemplate.getBytes,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )

    // Make hook executable
    val permissions = Set(
      PosixFilePermission.OWNER_READ,
      PosixFilePermission.OWNER_WRITE,
      PosixFilePermission.OWNER_EXECUTE,
      PosixFilePermission.GROUP_READ,
      PosixFilePermission.GROUP_EXECUTE,
      PosixFilePermission.OTHERS_READ,
      PosixFilePermission.OTHERS_EXECUTE
    )
    Files.setPosixFilePermissions(hookFile, permissions.asJava)
  }
}
