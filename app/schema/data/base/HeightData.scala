package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, HeightData => protoHeightData}
import schema.data.{Data, ListableData}
import schema.id.base.{DataID, HashID, StringID}
import schema.types.Height

case class HeightData(value: Height) extends ListableData {
  def getType: StringID = schema.constants.Data.HeightDataTypeID

  def getBondWeight: Int = schema.constants.Data.HeightDataWeight

  def getDataID: DataID = DataID(typeID = schema.constants.Data.HeightDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = HeightData(Height(-1))

  def getBytes: Array[Byte] = this.value.getBytes

  def generateHashID: HashID = if (this.value.value == -1) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoHeightData: protoHeightData = protoHeightData.newBuilder().setValue(this.value.asProtoHeight).build()

  def toAnyData: AnyData = AnyData.newBuilder().setHeightData(this.asProtoHeightData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setHeightData(this.asProtoHeightData).build()

  def getProtoBytes: Array[Byte] = this.asProtoHeightData.toByteString.toByteArray

  def viewString: String = "Height: " + this.value.AsString
}

object HeightData {

  def apply(value: protoHeightData): HeightData = HeightData(Height(value.getValue))

  def apply(protoBytes: Array[Byte]): HeightData = HeightData(protoHeightData.parseFrom(protoBytes))

  def apply(value: Long): HeightData = HeightData(Height(value))
}
