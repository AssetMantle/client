package schema.data.base

import com.data.{AnyData, BooleanData => protoBooleanData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

case class BooleanData(value: Boolean) extends Data {
  def getType: StringID = constants.Data.BooleanDataTypeID

  def getBondWeight: Int = constants.Data.BooleanBondWeight

  def getID: DataID = DataID(typeID = constants.Data.BooleanDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = BooleanData(false)

  def getBytes: Array[Byte] = {
    val res: Byte = if (this.value) 1 else 0
    Seq(res).toArray
  }

  def generateHashID: HashID = if (!this.value) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoBooleanData: protoBooleanData = protoBooleanData.newBuilder().setValue(this.value).build()

  def toAnyData: AnyData = AnyData.newBuilder().setBooleanData(this.asProtoBooleanData).build()

  def getProtoBytes: Array[Byte] = this.asProtoBooleanData.toByteString.toByteArray

  def viewString: String = this.value.toString
}

object BooleanData {

  def apply(value: protoBooleanData): BooleanData = BooleanData(value.getValue)

  def apply(protoBytes: Array[Byte]): BooleanData = BooleanData(protoBooleanData.parseFrom(protoBytes))
}