package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}

object UnconfirmedTxsResponse {

  case class Result(n_txs: String, total: String, total_bytes: String, txs: Seq[String])

  implicit val ResultReads: Reads[Result] = Json.reads[Result]

  case class Response(jsonrpc: String, id: Int, result: Result)

  implicit val ResponseReads: Reads[Response] = Json.reads[Response]

}
