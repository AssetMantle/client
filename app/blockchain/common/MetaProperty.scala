package blockchain.common

import models.common.Serializable
import play.api.libs.json.{Json, OWrites, Reads}

case class MetaProperty(value: MetaProperty.Value) {
  def toMetaProperty: Serializable.MetaProperty = Serializable.MetaProperty(id = value.id.value.idString, metaFact = value.metaFact.toMetaFact)
}

object MetaProperty {

  case class Value(id: ID, metaFact: MetaFact)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val metaPropertyReads: Reads[MetaProperty] = Json.reads[MetaProperty]
  implicit val metaPropertyWrites: OWrites[MetaProperty] = Json.writes[MetaProperty]

}



