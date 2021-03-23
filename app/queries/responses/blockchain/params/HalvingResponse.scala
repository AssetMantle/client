package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.HalvingParameter
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object HalvingResponse {

  case class Result(blockHeight: String) {
    def toParameter: Parameter = HalvingParameter(blockHeight = blockHeight.toInt)
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
