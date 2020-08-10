package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Properties(value: Properties.Value) {
  def toProperties: Serializable.Properties = Serializable.Properties(value.propertyList.fold[Seq[Serializable.Property]](Seq.empty)(_.map(_.toProperty)))
}

object Properties {

  case class Value(propertyList: Option[Seq[Property]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val propertiesReads: Reads[Properties] = Json.reads[Properties]

}
