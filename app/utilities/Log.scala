package utilities

import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.{Lang, MessagesApi}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class Log @Inject()(messagesApi: MessagesApi)
                   (implicit
                    executionContext: ExecutionContext,
                    configuration: Configuration
                   ) {
  
  private val language = configuration.get[String]("play.log.lang")

  def errorLog(failure: constants.Response.Failure, exception: Exception , logParameters: Any*)(implicit logger: Logger = Logger("Random Error Logger")) = {
    failure match {
      case constants.Response.NO_SUCH_ELEMENT_EXCEPTION => logger.error(messagesApi(failure.logMessage + "_" + logParameters.length.toString, logParameters: _*)(Lang(language)), exception)
      case constants.Response.PSQL_EXCEPTION => logger.error(messagesApi(failure.logMessage, logParameters: _*)(Lang(language)), exception)
    }
  }
}
