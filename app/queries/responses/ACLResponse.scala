package queries.responses

import models.blockchain.ACL
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ACLResponse {

  implicit val aclReads: Reads[ACL] = Json.reads[ACL]

  case class Value(address: String, zoneID: String, organizationID: String, acl: ACL)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
