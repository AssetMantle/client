package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import queries.responses.DocusignRegenerateTokenResponse.Response
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import java.util.Base64

import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignRegenerateToken @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_DOCUSIGN_REGENERATE_TOKEN

  private implicit val logger: Logger = Logger(this.getClass)

  private val oauthBasePath = configuration.get[String]("docusign.oauthBasePath")

  private val integrationKey = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_INTEGRATION_KEY)

  private val clientSecret = keyStore.getPassphrase(constants.KeyStore.DOCUSIGN_CLIENT_SECRET)

  private val headers = Tuple2("Authorization", "Basic " + Base64.getEncoder.encodeToString((integrationKey + ":" + clientSecret).getBytes))

  private def action(request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(oauthBasePath).withHttpHeaders(headers).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(grant_type: String, refresh_token: String)

  object Service {
    def post(request: Request): Future[Response] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}