package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, AssetID => protoAssetID}
import schema.id._

case class AssetID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def getType: StringID = constants.AssetIDType

  def asString: String = schema.utilities.common.base64URLEncoder(this.getBytes)

  def asProtoAssetID: protoAssetID = protoAssetID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setAssetID(this.asProtoAssetID).build()

  def getProtoBytes: Array[Byte] = this.asProtoAssetID.toByteString.toByteArray

  def compare(id: ID): Int = schema.utilities.common.byteArraysCompare(this.getBytes, id.asInstanceOf[AssetID].getBytes)
}

object AssetID {
  def apply(anyID: protoAssetID): AssetID = AssetID(HashID(anyID.getHashID))

  def apply(value: Array[Byte]): AssetID = AssetID(HashID(value))

  def apply(value: String): AssetID = AssetID(HashID(utilities.Secrets.base64URLDecode(value)))
}
