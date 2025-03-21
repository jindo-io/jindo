package config

import io.circe.yaml.parser
import java.io.InputStreamReader
import java.nio.file.{Files, Path}

class ConfigLoader(projectRoot: Path) {
  def loadConfig(): Config = {
    val configPath = projectRoot.resolve(".jindo.yaml")
    if (!Files.exists(configPath)) {
      throw new IllegalStateException(".jindo.yaml not found in project root")
    }

    val configStream = Files.newInputStream(configPath)
    val yamlJson = parser.parse(new InputStreamReader(configStream))

    yamlJson match {
      case Right(json) =>
        json.as[Config] match {
          case Right(config) => config
          case Left(error) =>
            throw new IllegalArgumentException(
              s"Invalid configuration: ${error.getMessage}"
            )
        }
      case Left(error) => throw error
    }
  }
}
