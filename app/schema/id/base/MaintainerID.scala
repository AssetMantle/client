package schema.id.base

import com.ids.{AnyID, MaintainerID => protoMaintainerID}
import schema.id.ID

case class MaintainerID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def asString: String = utilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoMaintainerID: protoMaintainerID = protoMaintainerID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setMaintainerID(this.asProtoMaintainerID).build()

  def getProtoBytes: Array[Byte] = this.asProtoMaintainerID.toByteString.toByteArray

}

object MaintainerID {
  def apply(anyID: protoMaintainerID): MaintainerID = MaintainerID(HashID(anyID.getHashID))
}