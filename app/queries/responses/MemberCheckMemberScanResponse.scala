package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object MemberCheckMemberScanResponse {

  case class DecisionDetail(text: String, matchDecision: String, assessedRisk: String, comment: String)

  implicit val decisionDetailReads: Reads[DecisionDetail] = Json.reads[DecisionDetail]

  case class ScanEntity(resultId: Int, uniqueId: Option[Int], monitoringStatus: Option[String], category: Option[String], firstName: String, middleName: Option[String],
                        lastName: String, matchRate: Option[Int], dob: Option[String], primaryLocation: Option[String], decisionDetail: Option[DecisionDetail])

  implicit val scanEntityReads: Reads[ScanEntity] = Json.reads[ScanEntity]

  case class ScanResult(scanID: Int, resultUrl: String, matchedNumber: Int, matchedEntities: Option[Seq[ScanEntity]])

  implicit val scanResult: Reads[ScanResult] = Json.reads[ScanResult]

  case class ScanInputParam(matchType: String, closeMatchRateThreshold: Int, whitelist: String, residence: String,
                            pepJurisdiction: String, memberNumber: String, firstName: String, middleName: String, lastName: String,
                            originalScriptName: String, gender: String, dob: String, address: String, updateMonitoringList: Boolean)

  implicit val scanInputParamResult: Reads[ScanInputParam] = Json.reads[ScanInputParam]

  case class Response(ScanInputParam: ScanInputParam, scanResult: ScanResult) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
