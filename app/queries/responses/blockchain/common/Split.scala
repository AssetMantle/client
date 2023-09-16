package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._

case class Split(asset_i_d: AssetID, owner_i_d: IdentityID, value: String) {
  def toSplit: models.blockchain.Split = {
    models.blockchain.Split(ownerID = this.owner_i_d.toIdentityID.getBytes, assetID = this.asset_i_d.toAssetID.getBytes, ownerIDString = this.owner_i_d.toIdentityID.asString, assetIDString = this.asset_i_d.toAssetID.asString, value = BigInt(value))
  }
}

object Split {
  implicit val SplitReads: Reads[Split] = Json.reads[Split]

  case class Key(split_i_d: SplitID)

  implicit val keyReads: Reads[Key] = Json.reads[Key]

  case class Mappable(split: Split)

  implicit val metaMappableReads: Reads[Mappable] = Json.reads[Mappable]

  case class Record(key: Key, mappable: Mappable)

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Module(records: Seq[Record], parameter_list: ParameterList)

  implicit val metaReads: Reads[Module] = Json.reads[Module]
}