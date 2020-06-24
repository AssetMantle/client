package queries

import java.net.ConnectException

import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.NegotiationResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetNegotiation @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_NEGOTIATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "negotiation"

  private val url = ip + ":" + port + "/" + path + "/"
  //testURL
  //private val testURL = constants.Test.BASE_URL+routes.LoopBackController.getNegotiation("")

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).get)

  object Service {

    def get(negotiationID: String): Future[Response] = action(negotiationID).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}