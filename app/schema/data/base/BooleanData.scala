package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, BooleanData => protoBooleanData}
import schema.data._
import schema.id.base.{DataID, HashID, StringID}

case class BooleanData(value: Boolean) extends ListableData {
  def getType: StringID = constants.BooleanDataTypeID

  def getBondWeight: Int = constants.BooleanBondWeight

  def getDataID: DataID = DataID(typeID = constants.BooleanDataTypeID, hashID = this.generateHashID)

  def zeroValue: BooleanData = BooleanData(false)

  def getBytes: Array[Byte] = {
    val res: Byte = if (this.value) 1 else 0
    Seq(res).toArray
  }

  def generateHashID: HashID = if (!this.value) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoBooleanData: protoBooleanData = protoBooleanData.newBuilder().setValue(this.value).build()

  def toAnyData: AnyData = AnyData.newBuilder().setBooleanData(this.asProtoBooleanData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setBooleanData(this.asProtoBooleanData).build()

  def getProtoBytes: Array[Byte] = this.asProtoBooleanData.toByteString.toByteArray

  def viewString: String = "Boolean: " + this.value.toString
}

object BooleanData {

  def apply(value: protoBooleanData): BooleanData = BooleanData(value.getValue)

  def apply(protoBytes: Array[Byte]): BooleanData = BooleanData(protoBooleanData.parseFrom(protoBytes))
}