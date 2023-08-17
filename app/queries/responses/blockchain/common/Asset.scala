package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._

object Asset {

  case class Key(asset_i_d: AssetID)

  implicit val keyReads: Reads[Key] = Json.reads[Key]

  case class Mappable(asset: Document)

  implicit val assetMappableReads: Reads[Mappable] = Json.reads[Mappable]

  case class Record(key: Key, mappable: Mappable)

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Module(records: Seq[Record], parameter_list: ParameterList)

  implicit val assetModuleReads: Reads[Module] = Json.reads[Module]
}
