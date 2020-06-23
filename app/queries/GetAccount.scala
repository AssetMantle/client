package queries

import java.net.ConnectException

import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.AccountResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAccount @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "auth/accounts"

  private val url = ip + ":" + port + "/" + path + "/"
  private val testURL = constants.Test.BASE_URL+routes.LoopBackController.getAccount("")
  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(testURL + request).get)

  object Service {

    def get(address: String): Future[Response] = action(address).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}