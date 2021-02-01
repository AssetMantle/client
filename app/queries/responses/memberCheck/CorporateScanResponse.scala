package queries.responses.memberCheck

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object CorporateScanResponse {

  case class DecisionDetail(text: String, matchDecision: String, assessedRisk: String, comment: String)

  implicit val decisionDetailReads: Reads[DecisionDetail] = Json.reads[DecisionDetail]
  implicit val decisionDetailWrites: OWrites[DecisionDetail] = Json.writes[DecisionDetail]

  case class ScanEntity(resultId: Int, uniqueId: Option[Int], monitoringStatus: Option[String], category: Option[String],
                        name: String, primaryLocation: Option[String], decisionDetail: Option[DecisionDetail])

  implicit val scanEntityReads: Reads[ScanEntity] = Json.reads[ScanEntity]
  implicit val scanEntityWrites: OWrites[ScanEntity] = Json.writes[ScanEntity]

  case class ScanResult(scanId: Int, resultUrl: String, matchedNumber: Int, matchedEntities: Option[Seq[ScanEntity]])

  implicit val scanResultReads: Reads[ScanResult] = Json.reads[ScanResult]
  implicit val scanResultWrites: OWrites[ScanResult] = Json.writes[ScanResult]

  case class ScanInputParam(matchType: String, whitelist: String, companyName: String,
                            idNumber: String, entityNumber: String, address: String, updateMonitoringList: Boolean)

  implicit val scanInputParamReads: Reads[ScanInputParam] = Json.reads[ScanInputParam]
  implicit val scanInputParamWrites: OWrites[ScanInputParam] = Json.writes[ScanInputParam]

  case class Response(scanParam: ScanInputParam, scanResult: ScanResult) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

}
