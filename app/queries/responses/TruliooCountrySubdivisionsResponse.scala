package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooCountrySubdivisionsResponse {

  case class Response(Name: String, Code: String, ParentCode : String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
