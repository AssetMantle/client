package schema.data.base

import com.data.{AnyData, AccAddressData => protoAccAddressData}
import com.google.protobuf.ByteString
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

case class AccAddressData(value: Array[Byte]) extends Data {
  def getType: StringID = constants.DataTypeID.AccAddressDataTypeID

  def getID: DataID = DataID(typeID = constants.DataTypeID.AccAddressDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = AccAddressData(new Array[Byte](0))

  def getBytes: Array[Byte] = this.value

  def generateHashID: HashID = utilities.ID.generateHashID(this.getBytes)

  def toBech32Address: String = utilities.Crypto.convertAccountAddressBytesToBech32Address(this.value)

  def asProtoAccAddressData: protoAccAddressData = protoAccAddressData.newBuilder().setValue(ByteString.copyFrom(this.value)).build()

  def toAnyData: AnyData = AnyData.newBuilder().setAccAddressData(this.asProtoAccAddressData).build()

  def getProtoBytes: Array[Byte] = this.asProtoAccAddressData.toByteString.toByteArray

  def viewString: String = this.toBech32Address
}

object AccAddressData {

  def apply(value: protoAccAddressData): AccAddressData = AccAddressData(value.getValue.toByteArray)

  def fromProtoBytes(protoBytes: Array[Byte]): AccAddressData = AccAddressData(protoAccAddressData.parseFrom(protoBytes))
}
