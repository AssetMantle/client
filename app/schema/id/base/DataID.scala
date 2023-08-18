package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, DataID => protoDataID}
import schema.data.constants._
import schema.id._

case class DataID(typeID: StringID, hashID: HashID) extends ID {

  def getDataTypeID: StringID = this.typeID

  def getType: StringID = constants.DataIDType

  def getBondWeight: Int = if (constants.AllIdTypes.contains(this.typeID)) IDDataWeight
  else this.typeID.value match {
    case AccAddressDataTypeID.value => AccAddressBondWeight
    case BooleanDataTypeID.value => BooleanBondWeight
    case DecDataTypeID.value => DecDataWeight
    case HeightDataTypeID.value => HeightDataWeight
    case ListDataTypeID.value => ListDataWeight
    case NumberDataTypeID.value => NumberDataWeight
    case StringDataTypeID.value => StringDataWeight
    case AnyDataTypeID.value => AnyDataWeight
    case _ => throw new IllegalArgumentException("unable to get bond weight - unknown type id: " + typeID)
  }

  def getHashID: HashID = this.hashID

  def getHashIDString: String = schema.utilities.common.base64URLEncoder(hashID.getBytes)

  def getBytes: Array[Byte] = this.typeID.getBytes ++ this.hashID.getBytes

  def asString: String = this.getDataTypeID.asString + constants.Separator + this.getHashIDString

  def asProtoDataID: protoDataID = protoDataID.newBuilder().setTypeID(this.typeID.asProtoStringID).setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setDataID(this.asProtoDataID).build()

  def getProtoBytes: Array[Byte] = this.asProtoDataID.toByteString.toByteArray

  def compare(id: ID): Int = schema.utilities.common.byteArraysCompare(this.getBytes, id.asInstanceOf[DataID].getBytes)
}

object DataID {
  def apply(anyID: protoDataID): DataID = DataID(typeID = StringID(anyID.getTypeID), hashID = HashID(anyID.getHashID))
}