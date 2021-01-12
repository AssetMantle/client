package queries.responses.blockchain

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import queries.responses.common.Header
import transactions.Abstract.BaseResponse

object WSClientBlockResponse {

  case class Block(header: Header)

  implicit val blockReads: Reads[Block] = Json.reads[Block]

  case class Value(block: Block)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Data(value: Value)

  implicit val dataReads: Reads[Data] = Json.reads[Data]

  //Removed proposer rewards event as it's already included in rewards event and it's not guaranteed it will be present (x/distribution/keeper/allocation.go:56)
  case class NewBlockEvents(slashAddress: Option[Seq[String]], slashReason: Option[Seq[String]], slashJailed: Option[Seq[String]], mintBondedRatio: Seq[String], mintAnnualProvisions: Seq[String], mintAmount: Seq[String], mintInflation: Seq[String], rewardsValidator: Seq[String], rewardsAmount: Seq[String], commissionAmount: Seq[String], commissionValidator: Seq[String], livenessAddress: Option[Seq[String]], livenessMissedBlocksCounter: Option[Seq[String]], livenessHeight: Option[Seq[String]])

  implicit val newBlockEventsReads: Reads[NewBlockEvents] = (
    (JsPath \ "slash.address").readNullable[Seq[String]] and
      (JsPath \ "slash.reason").readNullable[Seq[String]] and
      (JsPath \ "slash.jailed").readNullable[Seq[String]] and
      (JsPath \ "mint.bonded_ratio").read[Seq[String]] and
      (JsPath \ "mint.annual_provisions").read[Seq[String]] and
      (JsPath \ "mint.amount").read[Seq[String]] and
      (JsPath \ "mint.inflation").read[Seq[String]] and
      (JsPath \ "rewards.validator").read[Seq[String]] and
      (JsPath \ "rewards.amount").read[Seq[String]] and
      (JsPath \ "commission.amount").read[Seq[String]] and
      (JsPath \ "commission.validator").read[Seq[String]] and
      (JsPath \ "liveness.address").readNullable[Seq[String]] and
      (JsPath \ "liveness.missed_blocks").readNullable[Seq[String]] and
      (JsPath \ "liveness.height").readNullable[Seq[String]]
    ) (NewBlockEvents)

  case class Result(data: Data, events: NewBlockEvents)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
