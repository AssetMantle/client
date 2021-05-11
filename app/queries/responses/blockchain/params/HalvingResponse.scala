package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.HalvingParameter
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object HalvingResponse {

  case class Params(blockHeight: String) {
    def toParameter: Parameter = HalvingParameter(blockHeight = blockHeight.toInt)
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
