package schema.data.base

import com.data.{AnyData, HeightData => protoHeightData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}
import schema.types.Height

case class HeightData(value: Height) extends Data {
  def getType: StringID = constants.Data.HeightDataTypeID

  def getBondWeight: Int = constants.Data.HeightDataWeight

  def getDataID: DataID = DataID(typeID = constants.Data.HeightDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = HeightData(Height(-1))

  def getBytes: Array[Byte] = this.value.getBytes

  def generateHashID: HashID = if (this.value.value == -1) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoHeightData: protoHeightData = protoHeightData.newBuilder().setValue(this.value.asProtoHeight).build()

  def toAnyData: AnyData = AnyData.newBuilder().setHeightData(this.asProtoHeightData).build()

  def getProtoBytes: Array[Byte] = this.asProtoHeightData.toByteString.toByteArray

  def viewString: String = this.value.AsString
}

object HeightData {

  def apply(value: protoHeightData): HeightData = HeightData(Height(value.getValue))

  def apply(protoBytes: Array[Byte]): HeightData = HeightData(protoHeightData.parseFrom(protoBytes))
}
