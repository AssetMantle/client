package queries.responses.coingecko

import play.api.libs.json.{Json, Reads}


object TickerResponse {
  case class Data(usd: Double)

  implicit val dataReads: Reads[Data] = Json.reads[Data]

  case class Response(assetmantle: Data)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
