package jindo.infrastructure.coursier

import coursier._
import jindo.domain.error.JindoError
import scala.util.{Try, Success, Failure}

class DependencyResolver {

  /** Resolve dependencies and return classpath string */
  def resolve(
      dependencies: List[String],
      repositories: List[String] = List.empty
  ): Either[JindoError, String] = {
    for {
      modules <- parseDependencies(dependencies)
      repos <- parseRepositories(repositories)
      classpath <- resolveDependencies(modules, repos)
    } yield classpath
  }

  private def parseDependencies(
      dependencies: List[String]
  ): Either[JindoError, List[Dependency]] = {
    Try {
      dependencies.map { dep =>
        val parts = dep.split(":")
        if (parts.length < 3) {
          throw new IllegalArgumentException(
            s"Invalid dependency format: $dep (expected group:artifact:version)"
          )
        }

        val (group, artifact, version) = (parts(0), parts(1), parts(2))
        Dependency(Module(Organization(group), ModuleName(artifact)), version)
      }
    } match {
      case Success(modules) => Right(modules)
      case Failure(exception) =>
        Left(
          JindoError.DependencyResolutionError(
            "dependency parsing",
            exception.getMessage
          )
        )
    }
  }

  private def parseRepositories(
      repositories: List[String]
  ): Either[JindoError, List[Repository]] = {
    Try {
      val repos = if (repositories.nonEmpty) {
        repositories.map(MavenRepository(_))
      } else {
        List(Repositories.central)
      }
      repos
    } match {
      case Success(repos) => Right(repos)
      case Failure(exception) =>
        Left(
          JindoError.DependencyResolutionError(
            "repository parsing",
            exception.getMessage
          )
        )
    }
  }

  private def resolveDependencies(
      dependencies: List[Dependency],
      repositories: List[Repository]
  ): Either[JindoError, String] = {
    Try {
      val fetch = Fetch()
        .withDependencies(dependencies)
        .withRepositories(repositories)

      val files = fetch.run()
      files
        .map(_.getAbsolutePath)
        .mkString(System.getProperty("path.separator"))
    } match {
      case Success(classpath) => Right(classpath)
      case Failure(exception) =>
        Left(
          JindoError.DependencyResolutionError(
            "resolution",
            exception.getMessage
          )
        )
    }
  }
}
