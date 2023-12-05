package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo


object AllSigningInfosResponse {

  case class Response(info: Seq[SigningInfo.Result])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
