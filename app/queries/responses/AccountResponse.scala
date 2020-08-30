package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class PublicKey(value: String)

  implicit val publicKeyReads: Reads[PublicKey] = Json.reads[PublicKey]

  case class AccountValue(address: String, coins: Seq[Coin], public_key: Option[PublicKey], account_number: String, sequence: String)

  implicit val accountValueReads: Reads[AccountValue] = Json.reads[AccountValue]

  case class Result(value: AccountValue)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}