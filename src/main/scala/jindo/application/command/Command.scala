package jindo.application.command

import jindo.domain.error.JindoError

/** Base trait for all Jindo commands */
trait Command {

  /** Execute the command and return either success or error */
  def execute(): Either[JindoError, String]
}
