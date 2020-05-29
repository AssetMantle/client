package exceptions

import play.api.Logger

class BaseException(val failure: constants.Response.Failure, val exception: Exception = null)(implicit currentModule: String, logger: Logger) extends Exception {
  val module: String = currentModule
  logger.error(failure.logMessage, exception)
}
