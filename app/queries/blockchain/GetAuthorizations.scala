package queries.blockchain

import exceptions.BaseException
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.blockchain.AuthorizationsResponse.Response

import java.net.ConnectException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAuthorizations @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_BALANCE

  private implicit val logger: Logger = Logger(this.getClass)

  private val path1 = "cosmos/authz/v1beta1/grants?grantee="

  private val path2 = "&granter="

  private val url = constants.Blockchain.RestEndPoint + "/" + path1

  private def action(grantee: String, granter: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + grantee + path2 + granter).get)

  object Service {

    def get(grantee: String, granter: String): Future[Response] = action(grantee = grantee, granter = granter).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}