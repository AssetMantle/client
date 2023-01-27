package queries.keyBase

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.Configuration
import play.api.Logger
import queries.responses.keyBase.ValidatorKeyBaseAccountResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetValidatorKeyBaseAccount @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_VALIDATOR_KEY_BASE_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val path = "&fields=basics&fields=pictures"

  private val url = "https://keybase.io/_/api/1.0/user/lookup.json?key_suffix="

  private def action(identity: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + identity + path).get)

  object Service {

    def get(identity: String): Future[Response] = action(identity).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case baseException: BaseException => throw baseException
    }
  }

}
