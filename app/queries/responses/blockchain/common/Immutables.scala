package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}

case class Immutables(property_list: PropertyList) {
  def toImmutables: schema.qualified.Immutables = schema.qualified.Immutables(this.property_list.toPropertyList)
}

object Immutables {
  implicit val ImmutablesReads: Reads[Immutables] = Json.reads[Immutables]
}
