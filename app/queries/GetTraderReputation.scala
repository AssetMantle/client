package queries

import java.net.ConnectException

import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.TraderReputationResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTraderReputation @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRADER_REPUTATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "reputation"

  private val url = ip + ":" + port + "/" + path + "/"
  private val testURL = constants.Test.BASE_URL+routes.LoopBackController.getTraderReputation("")

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(testURL + request).get)

  object Service {

    def get(address: String): Future[Response] = action(address).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}