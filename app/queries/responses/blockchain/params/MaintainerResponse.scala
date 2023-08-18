package queries.responses.blockchain.params

import models.Abstract.Parameter
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ParameterList
import transactions.Abstract.BaseResponse

object MaintainerResponse {

  case class Response(result: ParameterList) extends BaseResponse {

    def toParameter: Parameter = this.result.getMaintainerParameter
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
