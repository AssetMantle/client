package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class Height(value: Height.Value) {
  def toInt: Int = value.height.toInt
}

object Height {

  case class Value(height: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val heightReads: Reads[Height] = Json.reads[Height]
}

