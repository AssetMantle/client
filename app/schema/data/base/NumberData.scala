package schema.data.base

import com.data.{AnyData, NumberData => protoNumberData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

import java.nio.{ByteBuffer, ByteOrder}

case class NumberData(value: Long) extends Data {
  def getType: StringID = constants.Data.NumberDataTypeID

  def getBondWeight: Int = constants.Data.NumberDataWeight

  def getDataID: DataID = DataID(typeID = constants.Data.NumberDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = NumberData(0)

  def getBytes: Array[Byte] = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(this.value).array()

  def generateHashID: HashID = if (this.value == 0) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoNumberData: protoNumberData = protoNumberData.newBuilder().setValue(this.value).build()

  def toAnyData: AnyData = AnyData.newBuilder().setNumberData(this.asProtoNumberData).build()

  def getProtoBytes: Array[Byte] = this.asProtoNumberData.toByteString.toByteArray

  def viewString: String = this.value.toString
}

object NumberData {

  def apply(value: protoNumberData): NumberData = NumberData(value.getValue)

  def apply(protoBytes: Array[Byte]): NumberData = NumberData(protoNumberData.parseFrom(protoBytes))
}
