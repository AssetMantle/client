package queries

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.BlockDetailsResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBlockDetails @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BLOCK_DETAILS

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.abciPort")

  private val minimumHeightQuery = "/blockchain?minHeight="

  private val maximumHeightQuery = "&maxHeight="

  private val url = ip + ":" + port + "/"

  private def action(minimumHeight: Int, maximumHeight: Int): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + minimumHeightQuery + minimumHeight.toString + maximumHeightQuery + maximumHeight.toString).get)

  object Service {

    def get(minimumHeight: Int, maximumHeight: Int): Future[Response] = action(minimumHeight = minimumHeight, maximumHeight = maximumHeight).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
    }
  }

}
