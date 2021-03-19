package queries.responses.common

import models.blockchain.{Redelegation => BlockchainRedelegation}
import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import utilities.MicroNumber

object Redelegation {

  case class Entry(creation_height: Int, completion_time: String, initial_balance: MicroNumber, shares_dst: String, balance: MicroNumber) {
    def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = initial_balance, sharesDestination = BigDecimal(shares_dst))
  }

  def entryApply(creation_height: Int, completion_time: String, initial_balance: String, shares_dst: String, balance: String): Entry = Entry(creation_height = creation_height, completion_time = completion_time, initial_balance = new MicroNumber(BigDecimal(initial_balance).toBigInt), shares_dst = shares_dst, balance = new MicroNumber(BigDecimal(balance).toBigInt))

  implicit val entryReads: Reads[Entry] = (
    (JsPath \ "creation_height").read[Int] and
      (JsPath \ "completion_time").read[String] and
      (JsPath \ "initial_balance").read[String] and
      (JsPath \ "shares_dst").read[String] and
      (JsPath \ "balance").read[String]
    ) (entryApply _)

  case class Redelegation(delegator_address: String, validator_src_address: String, validator_dst_address: String)

  implicit val redelegationReads: Reads[Redelegation] = Json.reads[Redelegation]

  case class Result(redelegation: Redelegation, entries: Seq[Entry]) {
    def toRedelegation: BlockchainRedelegation = BlockchainRedelegation(delegatorAddress = redelegation.delegator_address, validatorSourceAddress = redelegation.validator_src_address, validatorDestinationAddress = redelegation.validator_dst_address, entries = entries.map(_.toRedelegationEntry))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
