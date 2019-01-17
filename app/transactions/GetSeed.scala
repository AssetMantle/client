package transactions

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GetSeed @Inject()(configuration: Configuration, wsClient: WSClient, executionContext: ExecutionContext) {

  private val ip = configuration.get[String]("blockchain.main.ip")

  private val port = configuration.get[String]("blockchain.main.port")

  private val path = "keys/seed"

  private val url = ip + ":" + port + "/" + path

  private def action(): Future[Response] = wsClient.url(url).get.map { response => new Response(response) }(executionContext)

  class Response(response: WSResponse) {
    val body: String = response.body
  }

  object Service {
    def get(): Response = Await.result(action(), 1.seconds)
  }

}