package schema.data.base

import com.data.{AnyData, DecData => protoDecData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}
import utilities.AttoNumber

case class DecData(value: AttoNumber) extends Data {
  def getType: StringID = constants.Data.DecDataTypeID

  def getBondWeight: Int = constants.Data.DecDataWeight

  def getID: DataID = DataID(typeID = constants.Data.DecDataTypeID, hashID = this.generateHashID)

  def zeroValue: Data = DecData(AttoNumber.zero)

  def getBytes: Array[Byte] = this.value.getSortableDecBytes

  def generateHashID: HashID = if (this.value == AttoNumber.zero) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoDecData: protoDecData = protoDecData.newBuilder().setValue(this.value.toString).build()

  def toAnyData: AnyData = AnyData.newBuilder().setDecData(this.asProtoDecData).build()

  def getProtoBytes: Array[Byte] = this.asProtoDecData.toByteString.toByteArray

  def viewString: String = this.value.toString
}

object DecData {

  def apply(value: protoDecData): DecData = DecData(AttoNumber(value.getValue))

  def apply(protoBytes: Array[Byte]): DecData = DecData(protoDecData.parseFrom(protoBytes))
}
