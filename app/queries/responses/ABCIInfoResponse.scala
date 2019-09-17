package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ABCIInfoResponse {

  case class Info(data: String, last_block_height: String, last_block_app_hash: String)

  implicit val infoReads: Reads[Info] = Json.reads[Info]

  case class Result(response: Info)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(jsonrpc: String, id: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
