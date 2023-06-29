package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, StringData => protoStringData}
import schema.data._
import schema.id.base.{DataID, HashID, StringID}

case class StringData(value: String) extends ListableData {
  def getType: StringID = constants.StringDataTypeID

  def getBondWeight: Int = constants.StringDataWeight

  def getDataID: DataID = DataID(typeID = constants.StringDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = StringData("")

  def getBytes: Array[Byte] = this.value.getBytes

  def generateHashID: HashID = schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoStringData: protoStringData = protoStringData.newBuilder().setValue(this.value).build()

  def toAnyData: AnyData = AnyData.newBuilder().setStringData(this.asProtoStringData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setStringData(this.asProtoStringData).build()

  def getProtoBytes: Array[Byte] = this.asProtoStringData.toByteString.toByteArray

  def viewString: String = "String: " + this.value
}

object StringData {

  def apply(value: protoStringData): StringData = StringData(value.getValue)

  def apply(protoBytes: Array[Byte]): StringData = StringData(protoStringData.parseFrom(protoBytes))
}