
package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, SplitID => protoSplitID}
import schema.id._

case class SplitID(assetID: AssetID, ownerID: IdentityID) extends ID {

  def getBytes: Array[Byte] = this.assetID.getBytes ++ this.ownerID.getBytes

  def getType: StringID = constants.SplitIDType

  def asString: String = this.assetID.asString + constants.Separator + this.ownerID.asString

  def asProtoSplitID: protoSplitID = protoSplitID.newBuilder().setAssetID(this.assetID.asProtoAssetID).setOwnerID(this.ownerID.asProtoIdentityID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setSplitID(this.asProtoSplitID).build()

  def getProtoBytes: Array[Byte] = this.asProtoSplitID.toByteString.toByteArray

  def compare(id: ID): Int = schema.utilities.common.byteArraysCompare(this.getBytes, id.asInstanceOf[SplitID].getBytes)
}

object SplitID {
  def apply(splitID: protoSplitID): SplitID = SplitID(assetID = AssetID(splitID.getAssetID),ownerID = IdentityID(splitID.getOwnerID))
}