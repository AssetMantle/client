package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.ValidatorKeyBaseAccountResponse.Response

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