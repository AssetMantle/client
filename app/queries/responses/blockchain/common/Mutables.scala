package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}

case class Mutables(property_list: PropertyList) {
  def toMutables: schema.qualified.Mutables = schema.qualified.Mutables(this.property_list.toPropertyList)
}

object Mutables {
  implicit val MutablesReads: Reads[Mutables] = Json.reads[Mutables]
}
