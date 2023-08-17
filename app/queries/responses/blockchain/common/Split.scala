package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._

case class Split(owner_i_d: IdentityID, ownable_i_d: AnyOwnableID, value: String) {
  def toSplit: models.blockchain.Split = {
    models.blockchain.Split(ownerID = this.owner_i_d.toIdentityID.getBytes, ownableID = this.ownable_i_d.toOwnableID.getBytes, protoOwnableID = this.ownable_i_d.toOwnableID.toAnyOwnableID.toByteArray, ownerIDString = this.owner_i_d.toIdentityID.asString, ownableIDString = this.ownable_i_d.toOwnableID.asString, value = BigInt(value))
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