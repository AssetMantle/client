package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import queries.responses.TruliooAuthenticationResponse.Response

@Singleton
class GetTruliooAuthentication @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_AUTHENTICATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = configuration.get[String]("trulioo.apiKeyValue")

  private val headers = Tuple2(apiKeyName,apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.authentication")

  private val url = baseURL + endpoint

  private def action: Future[Response] = wsClient.url(url).withHttpHeaders(headers).get.map { response => new Response(response) }

  object Service {

    def get(): Future[Response] = action.recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}