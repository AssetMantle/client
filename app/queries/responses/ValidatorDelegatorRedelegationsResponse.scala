package queries.responses

import models.blockchain.Redelegation
import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object ValidatorDelegatorRedelegationsResponse {

  case class Entry(creation_height: Int, completion_time: String, initial_balance: MicroNumber, shares_dst: BigDecimal, balance: MicroNumber) {
    def toRedelegationEntry: Serializable.RedelegationEntry = Serializable.RedelegationEntry(creationHeight = creation_height.toInt, completionTime = completion_time, initialBalance = initial_balance, sharesDestination = shares_dst)
  }

  object Entry {
    def apply(creation_height: Int, completion_time: String, initial_balance: String, shares_dst: BigDecimal, balance: String): Entry = new Entry(creation_height, completion_time, new MicroNumber(BigInt(initial_balance)), shares_dst, new MicroNumber(BigInt(balance)))
  }

  implicit val entryReads: Reads[Entry] = (
    (JsPath \ "creation_height").read[Int] and
      (JsPath \ "completion_time").read[String] and
      (JsPath \ "initial_balance").read[String] and
      (JsPath \ "shares_dst").read[BigDecimal] and
      (JsPath \ "balance").read[String]
    ) (Entry.apply _)

  case class Result(delegator_address: String, validator_src_address: String, validator_dst_address: String, entries: Seq[Entry]) {
    def toRedelegation: Redelegation = Redelegation(delegatorAddress = delegator_address, validatorSourceAddress = validator_src_address, validatorDestinationAddress = validator_dst_address, entries = entries.map(_.toRedelegationEntry))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Seq[Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
