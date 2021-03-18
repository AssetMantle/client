package blockchain.common

import models.common.Serializable
import play.api.libs.json.{Json, OWrites, Reads}

case class Property(value: Property.Value) {
  def toProperty: Serializable.Property = Serializable.Property(id = value.id.value.idString, fact = value.fact.toFact)
}

object Property {

  case class Value(id: ID, fact: Fact)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val propertyReads: Reads[Property] = Json.reads[Property]
  implicit val propertyWrites: OWrites[Property] = Json.writes[Property]

}


