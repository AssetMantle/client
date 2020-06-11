package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import queries.responses.DocusignRegenerateTokenResponse.Response
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import java.util.Base64

import services.KeyStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DocusignRegenerateToken @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_DOCUSIGN_REGENERATE_TOKEN

  private implicit val logger: Logger = Logger(this.getClass)

  private val oauthBasePath = configuration.get[String]("docusign.oauthBasePath")

  private def action(request: Request, headers: (String, String)): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(oauthBasePath).withHttpHeaders(headers).post(Json.toJson(request)))

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(grant_type: String, refresh_token: String)

  object Service {
    def post(request: Request): Future[Response] = {
      val docusignIntegrationKey = Future(keyStore.getPassphrase("docusignIntegrationKey"))
      val docusignClientSecret = Future(keyStore.getPassphrase("docusignClientSecret"))

      (for {
        docusignIntegrationKey <- docusignIntegrationKey
        docusignClientSecret <- docusignClientSecret
        response <- action(request, Tuple2("Authorization", "Basic " + Base64.getEncoder.encodeToString((docusignIntegrationKey + ":" + docusignClientSecret).getBytes)))
      } yield response
        ).recover {
        case baseException: BaseException => throw baseException
        case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
          throw new BaseException(constants.Response.CONNECT_EXCEPTION)
      }
    }
  }

}