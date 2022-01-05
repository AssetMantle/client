package queries.responses.coingecko

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TickerResponse {
  case class Data(usd: Double)

  implicit val dataReads: Reads[Data] = Json.reads[Data]

  case class Response(persistence: Data) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
