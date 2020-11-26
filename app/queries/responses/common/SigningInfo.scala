package queries.responses.common

import models.blockchain.{SigningInfo => BlockchainSigningInfo}
import play.api.libs.json.{Json, Reads}

object SigningInfo {

  case class Result(address: String, start_height: Option[String], index_offset: String, jailed_until: String, tombstoned: Option[Boolean], missed_blocks_counter: Option[String]) {
    def toSigningInfo: BlockchainSigningInfo = BlockchainSigningInfo(consensusAddress = address, startHeight = start_height.fold(0)(_.toInt), jailedUntil = jailed_until, tombstoned = tombstoned.getOrElse(false))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]

}
