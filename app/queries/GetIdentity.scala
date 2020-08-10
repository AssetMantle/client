package queries

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.IdentityResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetIdentity @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "identities/identities"

  private val url = ip + ":" + port + "/" + path + "/"

  private def action(id: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + id).get)

  object Service {

    def get(id: String): Future[Response] = action(id).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
    }
  }

}