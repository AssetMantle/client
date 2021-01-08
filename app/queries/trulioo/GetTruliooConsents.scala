package queries.trulioo

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.trulioo.TruliooConsentsResponse.Response
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTruliooConsents @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_CONSENTS

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = keyStore.getPassphrase(constants.KeyStore.TRULIOO_API_KEY_VALUE)

  private val headers = Tuple2(apiKeyName, apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.consents")

  private val url = baseURL + endpoint

  private def action(request: String): Future[Response] = wsClient.url(url + request).withHttpHeaders(headers).get.map { response => new Response(response) }

  object Service {

    def get(configurationName: String = "Identity Verification", countryCode: String): Future[Response] = action(configurationName + "/" + countryCode).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}