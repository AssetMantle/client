package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo


object SigningInfoResponse {

  case class Response(val_signing_info: SigningInfo.Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
