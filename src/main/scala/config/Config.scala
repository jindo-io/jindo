package config

import io.circe.{Decoder, DecodingFailure}
import io.circe.generic.semiauto._
import hook.{Hook, SystemHook, JvmHook}

/** Configuration for git hooks */
case class Config(
    preCommit: Option[List[Hook]] = None
)

object Config {
  implicit val hookDecoder: Decoder[Hook] = (c: io.circe.HCursor) => {
    for {
      id <- c.downField("id").as[Option[String]]
      args <- c.getOrElse[List[String]]("args")(List.empty)
      command <- c.downField("command").as[Option[String]]
      mainClass <- c.downField("main").as[Option[String]]
      repositories <- c.downField("repositories").as[Option[List[String]]]
      dependencies <- c.downField("dependencies").as[Option[List[String]]]
    } yield {
      (command, mainClass) match {
        case (Some(cmd), None) => SystemHook(id, args, cmd)
        case (None, Some(main)) =>
          JvmHook(id, args, main, repositories, dependencies)
        case _ =>
          throw DecodingFailure(
            "Either 'command' or 'main' must be specified",
            c.history
          )
      }
    }
  }

  implicit val configDecoder: Decoder[Config] = deriveDecoder[Config]
}
