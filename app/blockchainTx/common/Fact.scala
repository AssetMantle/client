package blockchainTx.common

import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Fact(value: Fact.Value) {
  def toFact: Serializable.Fact = Serializable.Fact(value.factType, value.hash)
}

object Fact {

  case class Value(factType: String, hash: String)

  implicit val valueReads: Reads[Value] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "hash").read[String]
    ) (Value.apply _)

  implicit val valueWrites: Writes[Value] = (value: Value) => Json.obj(
    "type" -> value.factType,
    "hash" -> value.hash
  )

  implicit val factReads: Reads[Fact] = Json.reads[Fact]
  implicit val factWrites: OWrites[Fact] = Json.writes[Fact]

}
