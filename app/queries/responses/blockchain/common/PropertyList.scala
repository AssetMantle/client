package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.Property._

case class PropertyList(any_properties: Seq[AnyProperty]) {
  def toPropertyList: schema.list.PropertyList = schema.list.PropertyList(this.any_properties.map(_.toProperty))
}

object PropertyList {
  implicit val PropertyListReads: Reads[PropertyList] = Json.reads[PropertyList]
}
