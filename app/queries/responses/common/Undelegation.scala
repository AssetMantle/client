package queries.responses.common

import models.blockchain.{Undelegation => BlockchainUndelegation}
import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import utilities.MicroNumber

object Undelegation {

  case class Entry(creation_height: String, completion_time: String, initial_balance: MicroNumber, balance: MicroNumber) {
    def toUndelegationEntry: Serializable.UndelegationEntry = Serializable.UndelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = initial_balance, balance = balance)
  }

  object Entry {
    def apply(creation_height: String, completion_time: String, initial_balance: String, balance: String): Entry = new Entry(creation_height, completion_time, new MicroNumber(BigInt(initial_balance)), new MicroNumber(BigInt(balance)))
  }

  implicit val entryReads: Reads[Entry] = (
    (JsPath \ "creation_height").read[String] and
      (JsPath \ "completion_time").read[String] and
      (JsPath \ "initial_balance").read[String] and
      (JsPath \ "balance").read[String]
    ) (Entry.apply _)

  case class Result(delegator_address: String, validator_address: String, entries: Seq[Entry]) {
    def toUndelegation: BlockchainUndelegation = BlockchainUndelegation(delegatorAddress = delegator_address, validatorAddress = validator_address, entries = entries.map(_.toUndelegationEntry))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]
}
