package queries.responses.blockchain

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object CommunityPoolResponse {

  case class Pool(denom: String, amount: MicroNumber)

  object Pool {
    def apply(denom: String, amount: String): Pool = new Pool(denom, new MicroNumber(BigDecimal(amount).toBigInt()))
  }

  implicit val poolReads: Reads[Pool] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (Pool.apply _)

  case class Response(pool: Seq[Pool]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
