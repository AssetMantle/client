package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, PropertyID => protoPropertyID}
import schema.id.ID

case class PropertyID(keyID: StringID, typeID: StringID) extends ID {

  def getBytes: Array[Byte] = this.keyID.getBytes ++ this.typeID.getBytes

  def getType: StringID = schema.constants.ID.PropertyIDType

  def asString: String = this.keyID.asString + schema.constants.ID.Separator + this.typeID.asString

  def asProtoPropertyID: protoPropertyID = protoPropertyID.newBuilder().setTypeID(this.typeID.asProtoStringID).setKeyID(this.keyID.asProtoStringID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setPropertyID(this.asProtoPropertyID).build()

  def getProtoBytes: Array[Byte] = this.asProtoPropertyID.toByteString.toByteArray
}

object PropertyID {
  def apply(anyID: protoPropertyID): PropertyID = PropertyID(typeID = StringID(anyID.getTypeID), keyID = StringID(anyID.getKeyID))
}