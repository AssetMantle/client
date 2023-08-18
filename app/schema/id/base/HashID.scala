package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, HashID => protoHashId}
import com.google.protobuf.ByteString
import schema.id._

case class HashID(value: Array[Byte]) extends ID {

  def getBytes: Array[Byte] = this.value

  def getType: StringID = constants.HashIDType

  def asString: String = schema.utilities.common.base64URLEncoder(this.getBytes)

  def asHexString: String = this.value.map("%02x".format(_)).mkString.toUpperCase

  def asProtoHashID: protoHashId = protoHashId.newBuilder().setIDBytes(ByteString.copyFrom(this.getBytes)).build()

  def toAnyID: AnyID = AnyID.newBuilder().setHashID(this.asProtoHashID).build()

  def getProtoBytes: Array[Byte] = this.asProtoHashID.toByteString.toByteArray

  def compare(id: ID): Int = schema.utilities.common.byteArraysCompare(this.getBytes, id.asInstanceOf[AssetID].getBytes)
}

object HashID {
  def apply(anyID: protoHashId): HashID = HashID(anyID.getIDBytes.toByteArray)
}
