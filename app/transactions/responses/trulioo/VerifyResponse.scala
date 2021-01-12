package transactions.responses.trulioo

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object VerifyResponse {

  case class Errors(Code: String, Message: String)

  implicit val errorsReads: Reads[Errors] = Json.reads[Errors]

  case class DatasourceField(FieldName: String, Status: String, FieldGroup: Option[String])

  implicit val dataSourceFieldReads: Reads[DatasourceField] = Json.reads[DatasourceField]

  case class AppendedField(FieldName: String, Data: String)

  implicit val appendedFieldReads: Reads[AppendedField] = Json.reads[AppendedField]

  case class DatasourceResult(DatasourceStatus: Option[String], DatasourceName: String, DatasourceFields: Seq[DatasourceField], AppendedFields: Option[Seq[AppendedField]], Errors: Option[Seq[Errors]], FieldGroups: Option[Seq[String]])

  implicit val dataSourceResultReads: Reads[DatasourceResult] = Json.reads[DatasourceResult]

  case class Rule(RuleName: String, Note: String)

  implicit val ruleReads: Reads[Rule] = Json.reads[Rule]

  case class Record(TransactionRecordID: String, RecordStatus: String, DatasourceResults: Seq[DatasourceResult], Errors: Option[Seq[Errors]], Rule: Rule)

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Response(TransactionID: String, UploadedDt: String, CountryCode: String, ProductName: String, Record: Record, CustomerReferenceID: Option[String], Errors: Seq[Errors]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
