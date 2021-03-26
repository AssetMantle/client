package blockchain.common

import models.common.Serializable
import play.api.libs.json.{Json, OWrites, Reads}

case class Immutables(value: Immutables.Value) {
  def toImmutables: Serializable.Immutables = Serializable.Immutables(value.properties.toProperties)
}

object Immutables {

  case class Value(properties: Properties)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val immutablesReads: Reads[Immutables] = Json.reads[Immutables]
  implicit val immutablesWrites: OWrites[Immutables] = Json.writes[Immutables]

}