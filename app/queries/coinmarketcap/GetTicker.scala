package queries.coinmarketcap

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import queries.responses.coinmarketcap.TickerResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTicker @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_COINMARKETCAP_TICKER

  private implicit val logger: Logger = Logger(this.getClass)

  private val host = configuration.get[String]("blockchain.token.priceURL")

  // id=19686 is AssetMantle (MNTL); convertId=2781 is USD.
  private val path = "/cryptocurrency/quote/latest?id=19686&convertId=2781"

  private val url = host + path

  private def action(): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).get)

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}
