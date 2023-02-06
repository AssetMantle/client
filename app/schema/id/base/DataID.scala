package schema.id.base

import com.ids.{AnyID, DataID => protoDataID}
import schema.id.ID

case class DataID(typeID: StringID, hashID: HashID) extends ID {

  def getTypeID: StringID = this.typeID

  def getHashID: HashID = this.hashID

  def getHashIDString: String = utilities.Secrets.base64URLEncoder(hashID.getBytes)

  def getBytes: Array[Byte] = this.typeID.getBytes ++ this.hashID.getBytes

  def asString: String = this.getTypeID.asString + constants.Blockchain.IDSeparator + this.getHashIDString

  def asProtoDataID: protoDataID = protoDataID.newBuilder().setTypeID(this.typeID.asProtoStringID).setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setDataID(this.asProtoDataID).build()

  def getProtoBytes: Array[Byte] = this.asProtoDataID.toByteString.toByteArray
}

object DataID {
  def apply(anyID: protoDataID): DataID = DataID(typeID = StringID(anyID.getTypeID), hashID = HashID(anyID.getHashID))
}