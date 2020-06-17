package queries

import java.net.ConnectException

import controllers.routes
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.responses.MnemonicResponse.Response

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetMnemonic @Inject()()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_MNEMONIC

  private implicit val logger: Logger = Logger(this.getClass)

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.restPort")

  private val path = "keys/mnemonic"

  private val url = ip + ":" + port + "/" + path

  private val testUrl= constants.Test.BASE_URL+routes.LoopBackController.mnemonic
  println(testUrl)
  private def action(): Future[Response] = wsClient.url(testUrl).get.map { response => new Response(response) }

  object Service {
    def get(): Future[Response] = action().recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}
