package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.Property._

case class Parameter(meta_property: MetaProperty)

object Parameter {
  implicit val ParameterReads: Reads[Parameter] = Json.reads[Parameter]
}