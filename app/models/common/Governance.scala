package models.common

import play.api.libs.json._

object Governance {

  case class FinalTallyResult(yes: BigDecimal, abstain: BigDecimal, no: BigDecimal, noWithVeto: BigDecimal)

  implicit val finalTallyResultReads: Reads[FinalTallyResult] = Json.reads[FinalTallyResult]

  implicit val finalTallyResultWrites: OWrites[FinalTallyResult] = Json.writes[FinalTallyResult]


}
