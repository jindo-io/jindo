package jindo.domain.config

import io.circe.yaml.parser
import jindo.domain.error.JindoError
import java.nio.file.{Files, Path}
import scala.util.{Try, Success, Failure}

class ConfigLoader(projectRoot: Path) extends ConfigCodecs {

  private val configFileName = ".jindo.yaml"
  private val configPath = projectRoot.resolve(configFileName)

  /** Load and parse the Jindo configuration */
  def loadConfig(): Either[JindoError, JindoConfig] = {
    validateConfigExists()
      .flatMap(_ => readConfigFile())
      .flatMap(parseYaml)
      .flatMap(decodeConfig)
  }

  /** Check if configuration file exists */
  def configExists(): Boolean = Files.exists(configPath)

  /** Get the configuration file path */
  def getConfigPath: Path = configPath

  private def validateConfigExists(): Either[JindoError, Unit] = {
    if (configExists()) {
      Right(())
    } else {
      Left(JindoError.ConfigNotFound(configPath.toString))
    }
  }

  private def readConfigFile(): Either[JindoError, String] = {
    Try {
      val content = new String(Files.readAllBytes(configPath))
      content
    } match {
      case Success(content) => Right(content)
      case Failure(exception) =>
        Left(
          JindoError.ConfigParsingError(
            s"Failed to read config file: ${exception.getMessage}"
          )
        )
    }
  }

  private def parseYaml(content: String): Either[JindoError, io.circe.Json] = {
    parser.parse(content) match {
      case Right(json) => Right(json)
      case Left(error) =>
        Left(
          JindoError.ConfigParsingError(
            s"Invalid YAML format: ${error.getMessage}"
          )
        )
    }
  }

  private def decodeConfig(
      json: io.circe.Json
  ): Either[JindoError, JindoConfig] = {
    json.as[JindoConfig] match {
      case Right(config) => Right(config)
      case Left(error) =>
        Left(JindoError.InvalidConfiguration(error.getMessage))
    }
  }
}

object ConfigLoader {

  /** Create a config loader for the given project root */
  def apply(projectRoot: Path): ConfigLoader = new ConfigLoader(projectRoot)

  /** Load config from the current working directory */
  def loadFromCurrentDir(): Either[JindoError, JindoConfig] = {
    val currentDir = java.nio.file.Paths.get(".")
    new ConfigLoader(currentDir).loadConfig()
  }
}
