package transactions

import java.net.ConnectException

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequest
import responses.TruliooVerifyResponse.Response
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TruliooVerify @Inject()(wsClient: WSClient)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.TRANSACTIONS_TRULIOO_VERIFY

  private implicit val logger: Logger = Logger(this.getClass)

  private val apiKeyName = configuration.get[String]("trulioo.apiKeyName")

  private val apiKeyValue = configuration.get[String]("trulioo.apiKeyValue")

  private val headers = Tuple2(apiKeyName,apiKeyValue)

  private val baseURL = configuration.get[String]("trulioo.url")

  private val endpoint = configuration.get[String]("trulioo.endpoints.verify")

  private val url = baseURL + endpoint

  private def action(request: Request): Future[Response] = utilities.JSON.getResponseFromJson[Response](wsClient.url(url).withHttpHeaders(headers).post(Json.toJson(request)))

  private implicit val locationWrites: OWrites[Location] = Json.writes[Location]

  case class Location(StreetName: String, PostalCode: String)

  private implicit val personInfoWrites: OWrites[PersonInfo] = Json.writes[PersonInfo]

  case class PersonInfo(FirstGivenName: String, FirstSurName: String, DayOfBirth: Int, MonthOfBirth: Int, YearOfBirth: Int)

  private implicit val dataFieldsWrites: OWrites[DataFields] = Json.writes[DataFields]

  case class DataFields(PersonInfo: PersonInfo, Location: Option[JsValue], Communication: Option[JsValue], Passport: Option[JsValue])

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]

  case class Request(AcceptTruliooTermsAndConditions: Boolean, CleansedAddress: Boolean, ConfigurationName: String, ConsentForDataSources: Seq[String], CountryCode: String, DataFields: DataFields) extends BaseRequest

  object Service {

    def post(request: Request): Future[Response] = action(request).recover {
      case connectException: ConnectException => logger.error(constants.Response.CONNECT_EXCEPTION.message, connectException)
        throw new BaseException(constants.Response.CONNECT_EXCEPTION)
    }
  }

}