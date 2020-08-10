package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class Fact(value: Fact.Value) {
  def toFact: Serializable.Fact = Serializable.Fact(hash = value.hash, meta = value.meta)
}

object Fact {

  case class Value(hash: String, meta: Boolean)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val factReads: Reads[Fact] = Json.reads[Fact]
}
