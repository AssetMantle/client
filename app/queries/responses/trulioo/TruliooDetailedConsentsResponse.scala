package queries.responses.trulioo

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooDetailedConsentsResponse {

  case class Response(Name: String, Text: String, Url: Option[String]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

}
