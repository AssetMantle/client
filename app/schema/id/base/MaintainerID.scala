package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, MaintainerID => protoMaintainerID}
import schema.id._

case class MaintainerID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def getType: StringID = constants.MaintainerIDType

  def asString: String = utilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoMaintainerID: protoMaintainerID = protoMaintainerID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setMaintainerID(this.asProtoMaintainerID).build()

  def getProtoBytes: Array[Byte] = this.asProtoMaintainerID.toByteString.toByteArray

}

object MaintainerID {
  def apply(anyID: protoMaintainerID): MaintainerID = MaintainerID(HashID(anyID.getHashID))

  def apply(value: Array[Byte]): MaintainerID = MaintainerID(HashID(value))

  def apply(value: String): MaintainerID = MaintainerID(HashID(utilities.Secrets.base64URLDecode(value)))
}