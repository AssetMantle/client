package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class Attribute(key: String, value: Option[String])

object Attribute {
  implicit val attributeReads: Reads[Attribute] = Json.reads[Attribute]
}
