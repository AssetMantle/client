package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Trait(id: ID, property: Property, mutable: Boolean) {
  def toTrait: Serializable.Trait = Serializable.Trait(id = id.value.idString, property = property.toProperty, mutable = mutable)
}

object Trait {

  implicit val traitReads: Reads[Trait] = Json.reads[Trait]

}



