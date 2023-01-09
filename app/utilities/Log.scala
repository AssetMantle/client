package utilities

import javax.inject.{Inject, Singleton}
import play.api.i18n.{Lang, MessagesApi}
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext

@Singleton
class Log @Inject()(messagesApi: MessagesApi)
                   (implicit
                    executionContext: ExecutionContext,
                    configuration: Configuration
                   ) {

  private implicit val lang: Lang = Lang(configuration.get[String]("play.log.lang"))

  def infoLog(infoMessage: String, logParameters: Any*)(implicit logger: Logger): Unit = {
    logger.info(messagesApi(infoMessage, logParameters: _*))
  }
}
