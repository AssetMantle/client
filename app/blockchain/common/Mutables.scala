package blockchain.common

import models.common.Serializable
import play.api.libs.json.{Json, OWrites, Reads}

case class Mutables(value: Mutables.Value) {
  def toMutables: Serializable.Mutables = Serializable.Mutables(properties = value.properties.toProperties)
}

object Mutables {

  case class Value(properties: Properties)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val mutablesReads: Reads[Mutables] = Json.reads[Mutables]
  implicit val mutablesWrites: OWrites[Mutables] = Json.writes[Mutables]

}