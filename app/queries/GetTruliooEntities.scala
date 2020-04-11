package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import scala.concurrent.{ExecutionContext, Future}
import queries.responses.TruliooEntitiesResponse.Response

@Singleton
class GetTruliooEntities @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_ENTITIES

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = configuration.get[String]("trulioo.apiKeyValue")

  private val headers = Tuple2(apiKeyName, apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.entities")

  private val url = baseURL + endpoint

  private def action(request: String): Future[Seq[Response]] = wsClient.url(url + request).withHttpHeaders(headers).get.map { response => utilities.JSON.convertJsonStringToObject[Seq[Response]](response.body) }

  object Service {

    def get(configurationName: String = "Identity Verification", countryCode: String): Future[Seq[Response]] = action(configurationName + "/" + countryCode).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}