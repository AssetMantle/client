package queries.responses.blockchain.params

import models.Abstract.Parameter
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ParameterList


object SplitResponse {

  case class Response(result: ParameterList) {

    def toParameter: Parameter = this.result.getSplitParameter
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
