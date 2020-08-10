package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Mutables(value: Mutables.Value) {
  def toMutables: Serializable.Mutables = Serializable.Mutables(properties = value.properties.toProperties, maintainersID = value.maintainersID.value.idString)
}

object Mutables {

  case class Value(properties: Properties, maintainersID: ID)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val mutablesReads: Reads[Mutables] = Json.reads[Mutables]

}