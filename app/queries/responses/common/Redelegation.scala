package queries.responses.common

import models.blockchain.{Redelegation => BlockchainRedelegation}
import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import utilities.MicroNumber

case class Redelegation(delegator_address: String, validator_src_address: String, validator_dst_address: String)

object Redelegation {

  implicit val redelegationReads: Reads[Redelegation] = Json.reads[Redelegation]

  case class RedelegationEntry(creation_height: Int, completion_time: String, initial_balance: MicroNumber, shares_dst: String, balance: MicroNumber) {
    def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = initial_balance, sharesDestination = BigDecimal(shares_dst))
  }

  def redelegationEntryApply(creation_height: Int, completion_time: String, initial_balance: String, shares_dst: String, balance: String): RedelegationEntry = RedelegationEntry(creation_height = creation_height, completion_time = completion_time, initial_balance = new MicroNumber(BigDecimal(initial_balance).toBigInt), shares_dst = shares_dst, balance = new MicroNumber(BigDecimal(balance).toBigInt))

  implicit val entryReads: Reads[RedelegationEntry] = (
    (JsPath \ "creation_height").read[Int] and
      (JsPath \ "completion_time").read[String] and
      (JsPath \ "initial_balance").read[String] and
      (JsPath \ "shares_dst").read[String] and
      (JsPath \ "balance").read[String]
    ) (redelegationEntryApply _)

  case class Result(redelegation: Redelegation, entries: Seq[RedelegationEntry]) {
    def toRedelegation: BlockchainRedelegation = BlockchainRedelegation(delegatorAddress = redelegation.delegator_address, validatorSourceAddress = redelegation.validator_src_address, validatorDestinationAddress = redelegation.validator_dst_address, entries = entries.map(_.toRedelegationEntry))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
