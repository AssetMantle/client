package queries.Abstract

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.responses.blockchain.BlockResponse.tendermintEvidenceApply
import utilities.Blockchain.SlashingEvidence

abstract class TendermintEvidence {
  def getSlashingEvidences: Seq[SlashingEvidence]
}

object TendermintEvidence {
  implicit val tendermintEvidenceReads: Reads[TendermintEvidence] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (tendermintEvidenceApply _)
}
