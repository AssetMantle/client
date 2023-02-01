package schema.data.base

import com.data.{AnyData, IDData => protoIDData}
import com.ids.{AnyID, DataID => protoDataID}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}
import schema.id.{ID, base}

case class IDData(value: AnyID) extends Data {

  def asID: ID = ID(this.value)

  def getType: StringID = commonConstants.DataTypeID.IDDataTypeID

  def getID: DataID = base.DataID(typeID = commonConstants.DataTypeID.IDDataTypeID, hashID = this.generateHashID)

  def getBytes: Array[Byte] = this.asID.getBytes

  def generateHashID: HashID = commonUtilities.ID.generateHashID(this.getBytes)

  def getProtoDataID: protoDataID = this.getID.asProtoDataID

  def zeroValue: Data = IDData(StringID("").toAnyID)

  def asProtoIDData: protoIDData = protoIDData.newBuilder().setValue(this.value).build()

  def toAnyData: AnyData = AnyData.newBuilder().setIDData(this.asProtoIDData).build()

  def getProtoBytes: Array[Byte] = this.asProtoIDData.toByteString.toByteArray

  def viewString: String = this.asID.asString
}

object IDData {

  def apply(value: protoIDData): IDData = IDData(value.getValue)

  def apply(protoBytes: Array[Byte]): IDData = IDData(protoIDData.parseFrom(protoBytes))
}
