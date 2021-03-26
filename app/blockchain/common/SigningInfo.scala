package blockchain.common

import play.api.libs.json.{Json, Reads}

object SigningInfo {

  case class Result(address: String, start_height: Option[String], index_offset: String, jailed_until: String, tombstoned: Option[Boolean], missed_blocks_counter: Option[String])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

}
