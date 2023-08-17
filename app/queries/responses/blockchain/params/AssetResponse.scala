package queries.responses.blockchain.params

import models.Abstract.Parameter
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ParameterList
import transactions.Abstract.BaseResponse

object AssetResponse {

  case class Response(result: ParameterList) extends BaseResponse {

    def toParameter: Parameter = this.result.getAssetParameter
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
