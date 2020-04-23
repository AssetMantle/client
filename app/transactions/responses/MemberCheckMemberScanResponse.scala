package transactions.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object MemberCheckMemberScanResponse {

  case class Errors(Code: String, Message: String)

  implicit val errorsReads: Reads[Errors] = Json.reads[Errors]

  case class DatasourceField(FieldName: String, Status: String, FieldGroup: Option[String])

  implicit val dataSourceFieldReads: Reads[DatasourceField] = Json.reads[DatasourceField]

  case class AppendedField(FieldName: String, Data: String)

  implicit val appendedFieldReads: Reads[AppendedField] = Json.reads[AppendedField]

  case class DatasourceResult(DatasourceStatus: Option[String], DatasourceName: String, DatasourceFields: Seq[DatasourceField], AppendedFields: Option[Seq[AppendedField]], Errors: Option[Seq[Errors]], FieldGroups: Option[Seq[String]])

  implicit val dataSourceResultReads: Reads[DatasourceResult] = Json.reads[DatasourceResult]

  case class DecisionDetail(text: String, matchDecision: String, assessedRisk: String, comment: String)

  implicit val decisionDetailReads: Reads[DecisionDetail] = Json.reads[DecisionDetail]

  case class ScanEntity(resultId: Int, uniqueId: Option[Int], monitoringStatus: Option[String], category: Option[String], firstName: String, middleName: String,
                        lastName: String, matchRate: Option[Int], dob: Option[String], primaryLocation: Option[String], decisionDetail: Option[DecisionDetail])

  implicit val scanEntityReads: Reads[ScanEntity] = Json.reads[ScanEntity]

  case class Response(scanID: Int, resultUrl: String, matchedNumber: Int, matchedEntities: Seq[ScanEntity]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
