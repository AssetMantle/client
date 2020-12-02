package transactions.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object KeyResponse {

  case class KeyOutput(name: String, address: String, pubkey: String, mnemonic: String)

  implicit val keyOutputReads: Reads[KeyOutput] = Json.reads[KeyOutput]

  case class Result(success: Boolean, keyOutput: KeyOutput)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
