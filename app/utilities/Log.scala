package utilities

import javax.inject.{Inject, Singleton}
import play.api.i18n.{Lang, MessagesApi}
import play.api.{Configuration, Logger}
import scala.concurrent.ExecutionContext

@Singleton
class Log @Inject()(messagesApi: MessagesApi)
                   (implicit
                    executionContext: ExecutionContext,
                    configuration: Configuration
                   ) {

  private implicit val lang = Lang(configuration.get[String]("play.log.lang"))

  def infoLog(infoMessage: String, logParameters: Any*)(implicit logger: Logger) = {
    logger.info(messagesApi(infoMessage, logParameters: _*))
  }
}
