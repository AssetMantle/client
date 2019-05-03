package queries.responses

import play.api.libs.json.{Json, Reads}

object BlockHeightResponse {

  case class BlockHeader(height: String, time: String, num_txs: String, data_hash: Option[String], validators_hash: String, evidence_hash: Option[String])

  implicit val blockHeaderReads: Reads[BlockHeader] = Json.reads[BlockHeader]

  case class BlockID(hash: String)

  implicit val blockIDReads: Reads[BlockID] = Json.reads[BlockID]

  case class BlockMeta(block_id: BlockID, header: BlockHeader)

  implicit val blockMetaReads: Reads[BlockMeta] = Json.reads[BlockMeta]

  case class Result(block_meta: BlockMeta)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
