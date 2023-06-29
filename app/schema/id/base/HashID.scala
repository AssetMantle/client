package schema.id.base

import com.google.protobuf.ByteString
import com.assetmantle.schema.ids.base.{AnyID, HashID => protoHashId}
import schema.id._

case class HashID(value: Array[Byte]) extends ID {

  def getBytes: Array[Byte] = this.value

  def getType: StringID = constants.HashIDType

  def asString: String = utilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoHashID: protoHashId = protoHashId.newBuilder().setIDBytes(ByteString.copyFrom(this.getBytes)).build()

  def toAnyID: AnyID = AnyID.newBuilder().setHashID(this.asProtoHashID).build()

  def getProtoBytes: Array[Byte] = this.asProtoHashID.toByteString.toByteArray

}

object HashID {
  def apply(anyID: protoHashId): HashID = HashID(anyID.getIDBytes.toByteArray)
}
