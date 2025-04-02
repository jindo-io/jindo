package config

import io.circe.{Decoder, DecodingFailure}
import io.circe.generic.extras.semiauto._
import io.circe.Decoder.Result
import hook.{Hook, SystemHook, JvmHook}

trait ConfigCodecs {
  private def decodeHookFields(c: io.circe.HCursor) = {
    for {
      id <- c.downField("id").as[Option[String]]
      args <- c.getOrElse[List[String]]("args")(List.empty)
      command <- c.downField("command").as[Option[String]]
      mainClass <- c.downField("main").as[Option[String]]
      repositories <- c.downField("repositories").as[Option[List[String]]]
      dependencies <- c.downField("dependencies").as[Option[List[String]]]
    } yield (id, args, command, mainClass, repositories, dependencies)
  }

  implicit val hookDecoder: Decoder[Hook] = Decoder.instance { c =>
    decodeHookFields(c).flatMap {
      case (id, args, Some(cmd), None, _, _) =>
        Right(SystemHook(id, args, cmd))
      case (id, args, None, Some(main), repositories, dependencies) =>
        Right(JvmHook(id, args, main, repositories, dependencies))
      case _ =>
        Left(
          DecodingFailure(
            "Either 'command' or 'main' must be specified",
            c.history
          )
        )
    }
  }

  implicit val configDecoder: Decoder[Config] = Decoder.instance { c =>
    for {
      preCommit <- c.downField("pre-commit").as[Option[List[Hook]]]
    } yield Config(preCommit)
  }
}

object ConfigCodecs extends ConfigCodecs
