package queries

import java.net.{ConnectException, UnknownHostException}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import queries.responses.TruliooTransactionRecordResponse.Response
import utilities.KeyStore

@Singleton
class GetTruliooTransactionRecords @Inject()(wsClient: WSClient, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.QUERIES_GET_TRULIOO_TRANSACTION_RECORD

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = keyStore.getPassphrase(constants.KeyStore.TRULIOO_API_KEY_VALUE)

  private val headers = Tuple2(apiKeyName, apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.transactionRecord")

  private val url = baseURL + endpoint

  private def action(request: String): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url + request).withHttpHeaders(headers).get)

  object Service {

    def get(id: String): Future[Response] = action(id).recover {
      case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
      case unknownHostException: UnknownHostException => throw new BaseException(constants.Response.UNKNOWN_HOST_EXCEPTION, unknownHostException)
    }
  }

}