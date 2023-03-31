package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import schema.property.base.MetaProperty

case class ParameterList(parameters: Seq[Parameter]) {
  def getMetaProperty(propertyName: String): MetaProperty = this.parameters.find(_.meta_property.i_d.key_i_d.i_d_string == propertyName).fold(throw new IllegalArgumentException("property not found"))(_.meta_property.toMetaProperty)
}

object ParameterList {
  implicit val ParameterListReads: Reads[ParameterList] = Json.reads[ParameterList]
}
