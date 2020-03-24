package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooDataSourcesResponse {

  case class Field(FieldName:String, `type`: String)
  implicit val fieldReads: Reads[Field] = Json.reads[Field]

  case class Response(Name: String, Description: String, RequiredFields : Seq[Field], OptionalFields: Seq[Field], AppendedFields: Seq[Field], OutputFields: Seq[Field], SourceType: Seq[Field], UpdateFrequency: Option[String], Coverage: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
