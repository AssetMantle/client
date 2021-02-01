package transactions.responses.memberCheck

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object CorporateScanResponse {

  case class DecisionDetail(text: String, matchDecision: String, assessedRisk: String, comment: String)

  implicit val decisionDetailReads: Reads[DecisionDetail] = Json.reads[DecisionDetail]
  implicit val decisionDetailWrites: OWrites[DecisionDetail] = Json.writes[DecisionDetail]

  case class ScanEntity(resultId: Int, uniqueId: Option[Int], monitoringStatus: Option[String], category: Option[String], name: String,
                        primaryLocation: Option[String], decisionDetail: Option[DecisionDetail])

  implicit val scanEntityReads: Reads[ScanEntity] = Json.reads[ScanEntity]
  implicit val scanEntityWrites: OWrites[ScanEntity] = Json.writes[ScanEntity]

  case class Response(scanId: Int, resultUrl: String, matchedNumber: Int, matchedEntities: Option[Seq[ScanEntity]]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
