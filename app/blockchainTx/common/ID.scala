package blockchainTx.common

import play.api.libs.json.{Json, OWrites, Reads}

case class ID(value: ID.Value)

object ID {

  case class Value(idString: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val idReads: Reads[ID] = Json.reads[ID]

  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val idWrites: OWrites[ID] = Json.writes[ID]

}
