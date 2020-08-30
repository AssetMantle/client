package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class MetaProperties(value: MetaProperties.Value) {
  def toMetaProperties: Serializable.MetaProperties = Serializable.MetaProperties(value.metaPropertyList.fold[Seq[Serializable.MetaProperty]](Seq.empty)(_.map(_.toMetaProperty)))
}

object MetaProperties {

  case class Value(metaPropertyList: Option[Seq[MetaProperty]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val metaPropertiesReads: Reads[MetaProperties] = Json.reads[MetaProperties]

}
