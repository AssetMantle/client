package queries.responses.ascendex

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TickerResponse {
  case class Data(symbol: String, open: String, close: String, high: String, low: String, volume: String, ask: Seq[String], bid: Seq[String], `type`: String)

  implicit val dataReads: Reads[Data] = Json.reads[Data]

  case class Response(data: Data) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
