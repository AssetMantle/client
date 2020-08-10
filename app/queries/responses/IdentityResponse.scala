package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.{ID, Immutables, Mutables}
import transactions.Abstract.BaseResponse

object IdentityResponse {

  case class IdentityIDValue(chainID: ID, maintainersID: ID, classificationID: ID, hashID: ID)

  implicit val identityIDValueValueReads: Reads[IdentityIDValue] = Json.reads[IdentityIDValue]

  case class IdentityID(value: IdentityIDValue)

  implicit val identityIDReads: Reads[IdentityID] = Json.reads[IdentityID]

  case class Identity(id: IdentityID, provisionedAddressList: Option[Seq[String]], unprovisionedAddressList: Option[Seq[String]], immutables: Immutables, mutables: Mutables)

  implicit val identityReads: Reads[Identity] = Json.reads[Identity]

  case class IdentityValue(value: Identity)

  implicit val identityValueReads: Reads[IdentityValue] = Json.reads[IdentityValue]

  case class IdentitiesValue(id: ID, list: Seq[IdentityValue])

  implicit val identitiesValueReads: Reads[IdentitiesValue] = Json.reads[IdentitiesValue]

  case class Identities(value: IdentitiesValue)

  implicit val identitiesReads: Reads[Identities] = Json.reads[Identities]

  case class Value(identities: Identities)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}