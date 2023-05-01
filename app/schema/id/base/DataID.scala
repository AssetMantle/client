package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, DataID => protoDataID}
import schema.constants.Data._
import schema.id.ID

case class DataID(typeID: StringID, hashID: HashID) extends ID {

  def getDataTypeID: StringID = this.typeID

  def getType: StringID = schema.constants.ID.DataIDType

  def getBondWeight: Int = this.typeID.value match {
    case "A" => AccAddressBondWeight
    case "B" => BooleanBondWeight
    case "D" => DecDataWeight
    case "H" => HeightDataWeight
    case "I" => IDDataWeight
    case "L" => ListDataWeight
    case "N" => NumberDataWeight
    case "S" => StringDataWeight
    case _ => 0
  }

  def getHashID: HashID = this.hashID

  def getHashIDString: String = utilities.Secrets.base64URLEncoder(hashID.getBytes)

  def getBytes: Array[Byte] = this.typeID.getBytes ++ this.hashID.getBytes

  def asString: String = this.getDataTypeID.asString + schema.constants.ID.Separator + this.getHashIDString

  def asProtoDataID: protoDataID = protoDataID.newBuilder().setTypeID(this.typeID.asProtoStringID).setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setDataID(this.asProtoDataID).build()

  def getProtoBytes: Array[Byte] = this.asProtoDataID.toByteString.toByteArray
}

object DataID {
  def apply(anyID: protoDataID): DataID = DataID(typeID = StringID(anyID.getTypeID), hashID = HashID(anyID.getHashID))
}