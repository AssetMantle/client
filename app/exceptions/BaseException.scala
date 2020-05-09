package exceptions

import constants.Response.Failure
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{Lang, MessagesApi}

class BaseException @Inject()(val failure: constants.Response.Failure, val exception: Exception, logParameters: Any*)(implicit currentModule: String,messagesApi: MessagesApi, logger: Logger = Logger("Random Error Logger")) extends Exception {
  val module: String = currentModule
  failure.message match {
    case constants.Response.NO_SUCH_ELEMENT_EXCEPTION => logger.error(messagesApi(failure.logMessage + "_" + logParameters.length.toString, logParameters)(Lang("en")))
    case constants.Response.PSQL_EXCEPTION => logger.error(messagesApi(failure.logMessage, logParameters)(Lang("en")))
    case constants.Response.UNAUTHORIZED => logger.error(messagesApi(failure.logMessage, logParameters)(Lang("en")))
  }


  def this(failure: Failure){
    this(failure, new Exception)
  }

  def this(failure: Failure, exception: Exception){
    this(failure, exception)
  }
  //logger.error("Error Occured------------"+module+"---"+failure.message)
}
