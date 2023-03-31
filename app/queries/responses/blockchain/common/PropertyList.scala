package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.Property._

case class PropertyList(property_list: Seq[AnyProperty]) {
  def toPropertyList: schema.list.PropertyList = schema.list.PropertyList(this.property_list.map(_.toProperty))
}

object PropertyList {
  implicit val PropertyListReads: Reads[PropertyList] = Json.reads[PropertyList]
}
