package queries.responses.memberCheck

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object CorporateScanResponse {

  case class DecisionDetail(text: String, matchDecision: String, assessedRisk: String, comment: String)

  implicit val decisionDetailReads: Reads[DecisionDetail] = Json.reads[DecisionDetail]

  case class ScanEntity(resultId: Int, uniqueId: Option[Int], monitoringStatus: Option[String], category: Option[String],
                        name: String, primaryLocation: Option[String], decisionDetail: Option[DecisionDetail])

  implicit val scanEntityReads: Reads[ScanEntity] = Json.reads[ScanEntity]

  case class ScanResult(scanId: Int, resultUrl: String, matchedNumber: Int, matchedEntities: Option[Seq[ScanEntity]])

  implicit val scanResult: Reads[ScanResult] = Json.reads[ScanResult]

  case class ScanInputParam(matchType: String, whitelist: String, companyName: String,
                            idNumber: String, entityNumber: String, address: String, updateMonitoringList: Boolean)

  implicit val scanInputParamResult: Reads[ScanInputParam] = Json.reads[ScanInputParam]

  case class Response(scanParam: ScanInputParam, scanResult: ScanResult) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
