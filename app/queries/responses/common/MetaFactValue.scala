package queries.responses.common

import models.common.Serializable
import play.api.libs.json.{Json, Reads}

case class MetaFactValue(hash: String, fact: String) extends queries.`abstract`.FactValue{
  def toFactValue: Serializable.MetaFactValue = Serializable.MetaFactValue(hash = hash, fact = fact)
}

object MetaFactValue {

  implicit val metaFactValueReads: Reads[MetaFactValue] = Json.reads[MetaFactValue]
}

