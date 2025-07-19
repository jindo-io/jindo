package jindo.domain.hook

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HookSpec extends AnyFlatSpec with Matchers {

  "JvmHook" should "be valid with proper main class" in {
    val hook = JvmHook(mainClass = "com.example.MainClass")
    hook.isValid shouldBe true
  }

  it should "be invalid with empty main class" in {
    val hook = JvmHook(mainClass = "")
    hook.isValid shouldBe false
  }

  it should "be invalid with main class without package" in {
    val hook = JvmHook(mainClass = "MainClass")
    hook.isValid shouldBe false
  }

  it should "provide default repositories when none specified" in {
    val hook = JvmHook(mainClass = "com.example.Main")
    hook.getRepositories should contain("https://repo1.maven.org/maven2/")
  }

  it should "use custom repositories when specified" in {
    val customRepos =
      List("https://custom.repo.com/", "https://another.repo.com/")
    val hook = JvmHook(
      mainClass = "com.example.Main",
      repositories = Some(customRepos)
    )
    hook.getRepositories shouldBe customRepos
  }

  "SystemHook" should "be valid with non-empty command" in {
    val hook = SystemHook(command = "echo")
    hook.isValid shouldBe true
  }

  it should "be invalid with empty command" in {
    val hook = SystemHook(command = "")
    hook.isValid shouldBe false
  }

  it should "be invalid with whitespace-only command" in {
    val hook = SystemHook(command = "   ")
    hook.isValid shouldBe false
  }

  it should "build full command with arguments" in {
    val hook = SystemHook(
      command = "sbt",
      args = List("clean", "compile")
    )
    hook.fullCommand shouldBe List("sbt", "clean", "compile")
  }

  "Hook companion object" should "create JVM hook with defaults" in {
    val hook = Hook.jvm("com.example.Main")
    hook.mainClass shouldBe "com.example.Main"
    hook.args shouldBe List.empty
    hook.dependencies shouldBe None
    hook.id shouldBe None
  }

  it should "create JVM hook with all parameters" in {
    val hook = Hook.jvm(
      mainClass = "com.example.Main",
      dependencies = List("com.example:lib:1.0"),
      args = List("--option", "value"),
      id = Some("my-hook")
    )
    hook.mainClass shouldBe "com.example.Main"
    hook.dependencies shouldBe Some(List("com.example:lib:1.0"))
    hook.args shouldBe List("--option", "value")
    hook.id shouldBe Some("my-hook")
  }

  it should "create system hook with defaults" in {
    val hook = Hook.system("echo")
    hook.command shouldBe "echo"
    hook.args shouldBe List.empty
    hook.id shouldBe None
    hook.workingDirectory shouldBe None
  }

  it should "create system hook with all parameters" in {
    val hook = Hook.system(
      command = "sbt",
      args = List("test"),
      id = Some("test-hook"),
      workingDirectory = Some("/project/root")
    )
    hook.command shouldBe "sbt"
    hook.args shouldBe List("test")
    hook.id shouldBe Some("test-hook")
    hook.workingDirectory shouldBe Some("/project/root")
  }

  "Hook identifier" should "use id when available" in {
    val hook = JvmHook(id = Some("my-hook"), mainClass = "com.example.Main")
    hook.identifier shouldBe "my-hook"
  }

  it should "use class name when id not available" in {
    val hook = JvmHook(mainClass = "com.example.Main")
    hook.identifier shouldBe "JvmHook"
  }
}
