package exceptions

import play.api.Logger

class WSException(val failure: constants.Response.Failure, val exception: Exception = null, val errorMessage:String)(implicit currentModule: String, logger: Logger) extends Exception {
  val module: String = currentModule
  logger.error(failure.logMessage, exception)
}
