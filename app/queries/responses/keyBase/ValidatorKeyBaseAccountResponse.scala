package queries.responses.keyBase

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorKeyBaseAccountResponse {

  case class Status(code: Int, name: String)

  implicit val statusReads: Reads[Status] = Json.reads[Status]

  case class Primary(url: String)

  implicit val primaryReads: Reads[Primary] = Json.reads[Primary]

  case class Pictures(primary: Primary)

  implicit val picturesReads: Reads[Pictures] = Json.reads[Pictures]

  case class Basics(username: String)

  implicit val basicsReads: Reads[Basics] = Json.reads[Basics]

  case class Them(id: String, basics: Basics, pictures: Option[Pictures])

  implicit val themReads: Reads[Them] = Json.reads[Them]

  case class Response(status: Status, them: Seq[Them]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
