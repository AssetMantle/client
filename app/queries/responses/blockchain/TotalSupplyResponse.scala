package queries.responses.blockchain

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object TotalSupplyResponse {

  case class Result(denom: String, amount: MicroNumber)

  object Result {
    def apply(denom: String, amount: String): Result = new Result(denom, new MicroNumber(BigInt(amount)))
  }

  implicit val resultReads: Reads[Result] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (Result.apply _)

  case class Response(height: String, result: Seq[Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
