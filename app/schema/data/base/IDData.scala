package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, IDData => protoIDData}
import com.assetmantle.schema.ids.base.{AnyID, DataID => protoDataID}
import schema.data._
import schema.id.base.{DataID, HashID, StringID}
import schema.id.{ID, base}

case class IDData(value: ID) extends ListableData {

  def getID: ID = this.value

  def getBondWeight: Int = constants.IDDataWeight

  def getType: StringID = this.value.getType

  def getDataID: DataID = base.DataID(typeID = this.getType, hashID = this.generateHashID)

  def getBytes: Array[Byte] = this.getID.getBytes

  def generateHashID: HashID = schema.utilities.ID.generateHashID(this.getBytes)

  def getProtoDataID: protoDataID = this.getDataID.asProtoDataID

  def zeroValue: IDData = IDData(StringID(""))

  def asProtoIDData: protoIDData = protoIDData.newBuilder().setValue(this.value.toAnyID).build()

  def toAnyData: AnyData = AnyData.newBuilder().setIDData(this.asProtoIDData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setIDData(this.asProtoIDData).build()

  def getProtoBytes: Array[Byte] = this.asProtoIDData.toByteString.toByteArray

  def viewString: String = "ID: " + this.getID.asString

  def getAnyID: AnyID = this.value.toAnyID
}

object IDData {

  def apply(value: protoIDData): IDData = IDData(ID(value.getValue))

  def apply(protoBytes: Array[Byte]): IDData = IDData(protoIDData.parseFrom(protoBytes))
}
