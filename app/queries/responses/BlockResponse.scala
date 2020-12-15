package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Header
import transactions.Abstract.BaseResponse

object BlockResponse {

  case class Block(header: Header)

  implicit val blockReads: Reads[Block] = Json.reads[Block]

  case class Result(block: Block)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}