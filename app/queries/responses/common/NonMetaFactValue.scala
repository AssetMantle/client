package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class NonMetaFactValue(hash: String, meta: Boolean) extends queries.`abstract`.FactValue {
  def toFactValue: Serializable.NonMetaFactValue = Serializable.NonMetaFactValue(hash = hash, meta = meta)
}

object NonMetaFactValue {

  implicit val nonMetaFactValueReads: Reads[NonMetaFactValue] = Json.reads[NonMetaFactValue]
}
