package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Mutables(value: Mutables.Value) {
  def toMutables: Serializable.Mutables = Serializable.Mutables(properties = value.properties.toProperties)
}

object Mutables {

  case class Value(properties: Properties)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val mutablesReads: Reads[Mutables] = Json.reads[Mutables]

}