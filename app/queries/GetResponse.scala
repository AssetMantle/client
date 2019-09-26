package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GetResponse @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "response"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(request: String): Future[WSResponse] = wsClient.url(url + request).get

  object Service {

    def get(ticketID: String): WSResponse = {
      try {
        Await.result(action(ticketID), Duration.Inf)
      } catch {
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

}