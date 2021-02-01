package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class ID(value: ID.Value)

object ID {

  case class Value(idString: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val idReads: Reads[ID] = Json.reads[ID]

}
