package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class MetaProperty(value: MetaProperty.Value) {
  def toMetaProperty: Serializable.MetaProperty = Serializable.MetaProperty(id = value.id.value.idString, metaFact = value.metaFact.toMetaFact)
}

object MetaProperty {

  case class Value(id: ID, metaFact: MetaFact)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val metaPropertyReads: Reads[MetaProperty] = Json.reads[MetaProperty]

}



