package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import queries.responses.TruliooCountrySubdivisionsResponse.Response
import utilities.KeyStore


@Singleton
class GetTruliooCountrySubdivisions @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_COUNTRY_SUBDIVISIONS

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.countrySubdivisions")

  private val url = baseURL + endpoint

  private def action(request: String, headers: (String, String)): Future[Seq[Response]] = wsClient.url(url + request).withHttpHeaders(headers).get.map { response => utilities.JSON.convertJsonStringToObject[Seq[Response]](response.body) }

  object Service {

    def get(countryCode: String): Future[Seq[Response]] = {
      val truliooAPIKeyValue = Future(keyStore.getPassphrase("truliooAPIKeyValue"))

      (for {
        truliooAPIKeyValue <- truliooAPIKeyValue
        response <- action(countryCode, Tuple2(apiKeyName, truliooAPIKeyValue))
      } yield response
        ).recover {
        case baseException: BaseException => throw baseException
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }
}