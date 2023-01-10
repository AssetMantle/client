package queries.trulioo

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import queries.responses.trulioo.TruliooDetailedConsentsResponse.Response
import utilities.KeyStore

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetTruliooDetailedConsents @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_DETAILED_CONSENTS

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = keyStore.getPassphrase(constants.KeyStore.TRULIOO_API_KEY_VALUE)

  private val headers = Tuple2(apiKeyName, apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.detailedConsents")

  private val url = baseURL + endpoint

  private def action(request: String): Future[Seq[Response]] = wsClient.url(url + request).withHttpHeaders(headers).get.map { response => utilities.JSON.convertJsonStringToObject[Seq[Response]](response.body) }

  object Service {

    def get(configurationName: String = "Identity Verification", countryCode: String): Future[Seq[Response]] = action(configurationName + "/" + countryCode).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}