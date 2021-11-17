package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Fact(value: Fact.Value) {
  def toFact: Serializable.Fact = Serializable.Fact(value.`type`, value.hash)
}

object Fact {

  case class Value(`type`: String, hash: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val factReads: Reads[Fact] = Json.reads[Fact]

}
