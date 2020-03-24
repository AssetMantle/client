package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooTransactionRecordResponse {

  case class DatasourceField(FieldName: String, Status: String)
  implicit val dataSourceFieldReads: Reads[DatasourceField] = Json.reads[DatasourceField]

  case class DatasourceResult(DatasourceName: String, DatasourceFields: Seq[DatasourceField], AppendedFields: Option[Seq[DatasourceField]], Errors: Option[Seq[DatasourceField]], FieldGroups: Option[Seq[DatasourceField]])
  implicit val dataSourceResultReads: Reads[DatasourceResult] = Json.reads[DatasourceResult]

  case class Record(TransactionRecordID: String, RecordStatus: String, DatasourceResults: Seq[DatasourceResult])

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Field(FieldName: String, Value: String)

  implicit val fieldReads: Reads[Field] = Json.reads[Field]

  case class Response(InputFields: Seq[Field], UploadedDt: String, Record: Record, Errors: JsValue) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
