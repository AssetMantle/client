package queries.responses

import models.blockchain.ACL
import play.api.libs.json.{Json, OWrites, Reads}
import queries.responses.OrganizationResponse.Response
import transactions.Abstract.BaseResponse

object ACLResponse {

  implicit val aclReads: Reads[ACL] = Json.reads[ACL]
  implicit val aclWrites: OWrites[ACL] = Json.writes[ACL]

  case class Value(address: String, zoneID: String, organizationID: String, acl: ACL)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
