package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object BlockDetailsResponse {

  case class Header(chain_id: String, height: String, time: String, num_txs: String, total_txs: String, data_hash: String, evidence_hash: String, validators_hash: String)

  implicit val headerReads: Reads[Header] = Json.reads[Header]

  implicit val headerWrites: OWrites[Header] = Json.writes[Header]

  case class BlockID(hash: String)

  implicit val blockIDReads: Reads[BlockID] = Json.reads[BlockID]

  implicit val blockIDWrites: OWrites[BlockID] = Json.writes[BlockID]

  case class BlockMeta(block_id: BlockID, header: Header)

  implicit val blockMetaReads: Reads[BlockMeta] = Json.reads[BlockMeta]

  implicit val blockMetaWrites: OWrites[BlockMeta] = Json.writes[BlockMeta]

  case class Result(last_height: String, block_metas: Seq[BlockMeta])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  implicit val resultWrites: OWrites[Result] = Json.writes[Result]

  case class Response(jsonrpc: String, id: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

}
