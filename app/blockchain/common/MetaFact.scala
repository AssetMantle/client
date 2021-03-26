package blockchain.common

import models.common.Serializable
import play.api.libs.json.{Json, OWrites, Reads}

case class MetaFact(value: MetaFact.Value) {
  def toMetaFact: Serializable.MetaFact = Serializable.MetaFact(value.data.toData)
}

object MetaFact {

  case class Value(data: Data)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  implicit val metaFactValueReads: Reads[MetaFact] = Json.reads[MetaFact]
  implicit val metaFactWrites: OWrites[MetaFact] = Json.writes[MetaFact]
}

