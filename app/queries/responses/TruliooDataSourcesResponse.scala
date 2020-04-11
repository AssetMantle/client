package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooDataSourcesResponse {

  case class Field(FieldName:String, Type: String)
  implicit val fieldReads: Reads[Field] = Json.reads[Field]

  case class Response(Name: String, Description: Option[String], RequiredFields : Seq[Field], OptionalFields: Seq[Field], AppendedFields: Option[Seq[Field]], OutputFields: Seq[Field], SourceType: Option[String], UpdateFrequency: Option[String], Coverage: Option[String]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
