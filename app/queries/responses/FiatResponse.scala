package queries.responses

import play.api.libs.json.{Json, Reads}

object FiatResponse {

  case class Owners(ownerAddress: String, amount: Int)

  implicit val ownersReads: Reads[Owners] = Json.reads[Owners]

  case class Value(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, owners: Option[Seq[Owners]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
