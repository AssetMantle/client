package queries.ascendex

import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import queries.responses.ascendex.TickerResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTicker @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ASCENDEX_TICKER

  private implicit val logger: Logger = Logger(this.getClass)

  private val host = configuration.get[String]("blockchain.token.priceURL")

  private val path1 = "/ticker?symbol="

  private val path2 = "/USDT"

  private val url = host + path1

  private def action(tokenName: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + tokenName + path2).get)

  object Service {

    def get(tokenName: String): Future[Response] = action(tokenName: String).recover {
      case connectException: ConnectException => constants.Response.CONNECT_EXCEPTION.throwBaseException(connectException)
    }
  }

}