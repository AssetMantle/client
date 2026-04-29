package queries.responses.coinmarketcap

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TickerResponse {

  case class Quote(price: Double)

  implicit val quoteReads: Reads[Quote] = Json.reads[Quote]

  case class Coin(quotes: Seq[Quote])

  implicit val coinReads: Reads[Coin] = Json.reads[Coin]

  case class Response(data: Seq[Coin]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
