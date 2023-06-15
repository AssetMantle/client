package queries.coingecko

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.coingecko.TickerResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTicker @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_COINGECKO_TICKER

  private implicit val logger: Logger = Logger(this.getClass)

  private val host = configuration.get[String]("blockchain.token.priceURL")

  // Should not make id as config parameter as response structure has a key name same as id
  private val path = "/simple/price?ids=assetmantle&vs_currencies=usd"

  private val url = host + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}