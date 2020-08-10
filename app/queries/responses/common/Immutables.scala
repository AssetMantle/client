package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Immutables(value: Immutables.Value) {
  def toImmutables: Serializable.Immutables = Serializable.Immutables(value.properties.toProperties)
}

object Immutables {
  case class Value(properties: Properties)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val immutablesReads: Reads[Immutables] = Json.reads[Immutables]

}