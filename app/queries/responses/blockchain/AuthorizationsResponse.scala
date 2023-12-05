package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Authz._
import utilities.Date.RFC3339

object AuthorizationsResponse {

  case class Grant(authorization: Authorization, expiration: RFC3339)

  implicit val grantReads: Reads[Grant] = Json.reads[Grant]

  case class Response(grants: Seq[Grant])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
