package queries.responses.blockchain.params

import models.Abstract.Parameter
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ParameterList


object ClassificationResponse {

  case class Response(result: ParameterList) {
    def toParameter: Parameter = this.result.getClassificationParameter
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
