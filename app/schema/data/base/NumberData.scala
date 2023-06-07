package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, NumberData => protoNumberData}
import schema.data.{Data, ListableData}
import schema.id.base.{DataID, HashID, StringID}

case class NumberData(value: BigInt) extends ListableData {
  def getType: StringID = schema.constants.Data.NumberDataTypeID

  def getBondWeight: Int = schema.constants.Data.NumberDataWeight

  def getDataID: DataID = DataID(typeID = schema.constants.Data.NumberDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = NumberData(0)

  def getBytes: Array[Byte] = this.value.toString().getBytes

  def generateHashID: HashID = if (this.value == 0) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoNumberData: protoNumberData = protoNumberData.newBuilder().setValue(this.value.toString()).build()

  def toAnyData: AnyData = AnyData.newBuilder().setNumberData(this.asProtoNumberData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setNumberData(this.asProtoNumberData).build()

  def getProtoBytes: Array[Byte] = this.asProtoNumberData.toByteString.toByteArray

  def viewString: String = "Number: " + this.value.toString
}

object NumberData {

  def apply(value: protoNumberData): NumberData = NumberData(BigInt(value.getValue))

  def apply(protoBytes: Array[Byte]): NumberData = NumberData(protoNumberData.parseFrom(protoBytes))
}
