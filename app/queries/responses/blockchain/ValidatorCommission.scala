package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object ValidatorCommission {

  case class Commission(commission: Seq[Coin])

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

  case class Response(commission: Commission) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
