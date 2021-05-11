package queries.responses.blockchain

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object StakingPoolResponse {

  case class Pool(not_bonded_tokens: MicroNumber, bonded_tokens: MicroNumber)

  object Pool {
    def apply(not_bonded_tokens: String, bonded_tokens: String): Pool = new Pool(new MicroNumber(BigInt(not_bonded_tokens)), new MicroNumber(BigInt(bonded_tokens)))
  }

  implicit val poolReads: Reads[Pool] = (
    (JsPath \ "not_bonded_tokens").read[String] and
      (JsPath \ "bonded_tokens").read[String]
    ) (Pool.apply _)

  case class Response(pool: Pool) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
