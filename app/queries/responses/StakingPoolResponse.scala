package queries.responses

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object StakingPoolResponse {

  case class Result(not_bonded_tokens: MicroNumber, bonded_tokens: MicroNumber)

  object Result {
    def apply(not_bonded_tokens: String, bonded_tokens: String): Result = new Result(new MicroNumber(BigInt(not_bonded_tokens)), new MicroNumber(BigInt(bonded_tokens)))
  }

  implicit val resultReads: Reads[Result] = (
    (JsPath \ "not_bonded_tokens").read[String] and
      (JsPath \ "bonded_tokens").read[String]
    ) (Result.apply _)

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
