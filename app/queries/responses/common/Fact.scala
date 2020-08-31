package queries.responses.common

import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

case class Fact(value: Fact.Value) {
  def toFact: Serializable.Fact = Serializable.Fact(value.factType, value.hash)
}

object Fact {

  case class Value(factType: String, hash: String)

  implicit val valueReads: Reads[Value] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "hash").read[String]
    ) (Value.apply _)

  implicit val factReads: Reads[Fact] = Json.reads[Fact]

}
