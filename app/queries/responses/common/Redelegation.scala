package queries.responses.common

import models.blockchain.{Redelegation => BlockchainRedelegation}
import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import utilities.MicroNumber

case class Redelegation(delegator_address: String, validator_src_address: String, validator_dst_address: String)

object Redelegation {

  implicit val redelegationReads: Reads[Redelegation] = Json.reads[Redelegation]

  case class RedelegationEntry(creation_height: Int, completion_time: String, initial_balance: MicroNumber, shares_dst: String) {
    def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = initial_balance, sharesDestination = BigDecimal(shares_dst))
  }

  def redelegationEntryApply(creation_height: Int, completion_time: String, initial_balance: String, shares_dst: String): RedelegationEntry = RedelegationEntry(creation_height = creation_height, completion_time = completion_time, initial_balance = new MicroNumber(BigDecimal(initial_balance).toBigInt), shares_dst = shares_dst)

  implicit val redelegationEntryReads: Reads[RedelegationEntry] = (
    (JsPath \ "creation_height").read[Int] and
      (JsPath \ "completion_time").read[String] and
      (JsPath \ "initial_balance").read[String] and
      (JsPath \ "shares_dst").read[String]
    ) (redelegationEntryApply _)

  case class Entry(redelegation_entry: RedelegationEntry)

  implicit val entryReads: Reads[Entry] = Json.reads[Entry]

  case class Result(redelegation: Redelegation, entries: Seq[Entry]) {
    def toRedelegation: BlockchainRedelegation = BlockchainRedelegation(delegatorAddress = redelegation.delegator_address, validatorSourceAddress = redelegation.validator_src_address, validatorDestinationAddress = redelegation.validator_dst_address, entries = entries.map(_.redelegation_entry.toRedelegationEntry))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
