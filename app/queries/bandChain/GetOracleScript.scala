package queries.bandChain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.bandChain.OracleScriptResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetOracleScript @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BAND_ORACLE_SCRIPT

  private implicit val logger: Logger = Logger(this.getClass)

  private val host = configuration.get[String]("bandChain.url")

  private val path = "/rest/oracle/oracle_scripts/"

  private val url = host + path

  private def action(scriptID: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + scriptID).get)

  object Service {

    def get(scriptID: String): Future[Response] = action(scriptID).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}