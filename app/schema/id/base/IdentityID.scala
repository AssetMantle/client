package schema.id.base

import com.ids.{AnyID, IdentityID => protoIdentityID}
import schema.id.ID

case class IdentityID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def asString: String = utilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoIdentityID: protoIdentityID = protoIdentityID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setIdentityID(this.asProtoIdentityID).build()

  def getProtoBytes: Array[Byte] = this.asProtoIdentityID.toByteString.toByteArray

}

object IdentityID {
  def apply(anyID: protoIdentityID): IdentityID = IdentityID(HashID(anyID.getHashID))
}