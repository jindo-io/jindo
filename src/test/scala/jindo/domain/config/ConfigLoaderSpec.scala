package jindo.domain.config

import jindo.domain.error.JindoError
import jindo.domain.hook.{JvmHook, SystemHook}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues
import java.nio.file.{Files, Path, Paths}
import java.nio.charset.StandardCharsets

class ConfigLoaderSpec extends AnyFlatSpec with Matchers with EitherValues {

  "ConfigLoader" should "load valid configuration successfully" in {
    val tempDir = Files.createTempDirectory("jindo-test")
    val configContent =
      """
        |pre-commit:
        |  - id: scalafmt
        |    main: org.scalafmt.cli.Cli
        |    dependencies:
        |      - org.scalameta::scalafmt-cli:3.7.17
        |  - id: compile
        |    command: sbt
        |    args: ["compile"]
        |""".stripMargin

    val configFile = tempDir.resolve(".jindo.yaml")
    Files.write(configFile, configContent.getBytes(StandardCharsets.UTF_8))

    val loader = new ConfigLoader(tempDir)
    val result = loader.loadConfig()

    result.isRight shouldBe true
    val config = result.value
    config.preCommit should have size 2

    val jvmHook = config.preCommit.head.asInstanceOf[JvmHook]
    jvmHook.id shouldBe Some("scalafmt")
    jvmHook.mainClass shouldBe "org.scalafmt.cli.Cli"

    val systemHook = config.preCommit(1).asInstanceOf[SystemHook]
    systemHook.id shouldBe Some("compile")
    systemHook.command shouldBe "sbt"

    // Cleanup
    Files.deleteIfExists(configFile)
    Files.deleteIfExists(tempDir)
  }

  it should "return ConfigNotFound error when file doesn't exist" in {
    val nonExistentDir = Paths.get("/tmp/non-existent-dir")
    val loader = new ConfigLoader(nonExistentDir)
    val result = loader.loadConfig()

    result.isLeft shouldBe true
    result.left.value shouldBe a[JindoError.ConfigNotFound]
  }

  it should "return ConfigParsingError for invalid YAML" in {
    val tempDir = Files.createTempDirectory("jindo-test")
    val invalidYaml = "invalid: yaml: content: [unclosed"

    val configFile = tempDir.resolve(".jindo.yaml")
    Files.write(configFile, invalidYaml.getBytes(StandardCharsets.UTF_8))

    val loader = new ConfigLoader(tempDir)
    val result = loader.loadConfig()

    result.isLeft shouldBe true
    result.left.value shouldBe a[JindoError.ConfigParsingError]

    // Cleanup
    Files.deleteIfExists(configFile)
    Files.deleteIfExists(tempDir)
  }

  it should "return InvalidConfiguration for hooks with neither command nor main" in {
    val tempDir = Files.createTempDirectory("jindo-test")
    val invalidConfig =
      """
        |pre-commit:
        |  - id: invalid-hook
        |    args: ["some", "args"]
        |""".stripMargin

    val configFile = tempDir.resolve(".jindo.yaml")
    Files.write(configFile, invalidConfig.getBytes(StandardCharsets.UTF_8))

    val loader = new ConfigLoader(tempDir)
    val result = loader.loadConfig()

    result.isLeft shouldBe true
    result.left.value shouldBe a[JindoError.InvalidConfiguration]

    // Cleanup
    Files.deleteIfExists(configFile)
    Files.deleteIfExists(tempDir)
  }
}
