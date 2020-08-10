package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Property(value: Property.Value) {
  def toProperty: Serializable.Property = Serializable.Property(id = value.id.value.idString, fact = value.fact.toFact)
}

object Property {

  case class Value(id: ID, fact: Fact)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val propertyReads: Reads[Property] = Json.reads[Property]

}


