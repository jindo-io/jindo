package jindo.domain.config

import io.circe.{Decoder, DecodingFailure}
import jindo.domain.hook.{Hook, SystemHook, JvmHook}

trait ConfigCodecs {

  private def decodeHookFields(c: io.circe.HCursor) = {
    for {
      id <- c.downField("id").as[Option[String]]
      args <- c.getOrElse[List[String]]("args")(List.empty)
      command <- c.downField("command").as[Option[String]]
      mainClass <- c.downField("main").as[Option[String]]
      repositories <- c.downField("repositories").as[Option[List[String]]]
      dependencies <- c.downField("dependencies").as[Option[List[String]]]
      workingDirectory <- c.downField("working-directory").as[Option[String]]
    } yield (
      id,
      args,
      command,
      mainClass,
      repositories,
      dependencies,
      workingDirectory
    )
  }

  implicit val hookDecoder: Decoder[Hook] = Decoder.instance { c =>
    decodeHookFields(c).flatMap {
      case (id, args, Some(cmd), None, _, _, workingDir) =>
        val hook = SystemHook(id, args, cmd, workingDir)
        if (hook.isValid) Right(hook)
        else
          Left(
            DecodingFailure(
              s"Invalid system hook: command cannot be empty",
              c.history
            )
          )

      case (id, args, None, Some(main), repositories, dependencies, _) =>
        val hook = JvmHook(id, args, main, dependencies, repositories)
        if (hook.isValid) Right(hook)
        else
          Left(
            DecodingFailure(
              s"Invalid JVM hook: main class '$main' must be a valid class name",
              c.history
            )
          )

      case (_, _, Some(_), Some(_), _, _, _) =>
        Left(
          DecodingFailure(
            "Hook cannot have both 'command' and 'main' fields",
            c.history
          )
        )

      case _ =>
        Left(
          DecodingFailure(
            "Hook must have either 'command' (for system hook) or 'main' (for JVM hook)",
            c.history
          )
        )
    }
  }

  implicit val jindoConfigDecoder: Decoder[JindoConfig] = Decoder.instance {
    c =>
      for {
        preCommit <- c
          .downField("pre-commit")
          .as[Option[List[Hook]]]
          .map(_.getOrElse(List.empty))
      } yield JindoConfig(preCommit)
  }
}

object ConfigCodecs extends ConfigCodecs
