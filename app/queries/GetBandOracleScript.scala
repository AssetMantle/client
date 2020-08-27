package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.BandOracleScriptResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetBandOracleScript @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_META

  private implicit val logger: Logger = Logger(this.getClass)

  private val url = "guanyu-devnet.bandchain.org/rest/oracle/oracle_scripts/"

  private def action(scriptID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + scriptID).get)

  object Service {

    def get(scriptID: String): Future[Response] = action(scriptID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}