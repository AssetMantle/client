package queries.Abstract

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.responses.blockchain.BlockResponse.tendermintEvidenceApply
import utilities.Date.RFC3339
import utilities.MicroNumber

abstract class TendermintEvidence {
  val height: Int
  val timeStamp: RFC3339
  val validatorHexAddress: String
  val validatorPower: MicroNumber
  val totalVotingPower: MicroNumber
}

object TendermintEvidence {
  implicit val tendermintEvidenceReads: Reads[TendermintEvidence] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (tendermintEvidenceApply _)
}
