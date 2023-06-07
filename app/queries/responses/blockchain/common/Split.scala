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
}