package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object ValidatorOutstandingRewards {

  case class Response(height: String, result: Seq[Coin]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
