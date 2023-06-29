package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, StringID => protoStringID}
import schema.id.ID

// Should always be case class (not class) otherwise equality will fail
case class StringID(value: String) extends ID {

  def getBytes: Array[Byte] = this.value.getBytes

  // Cannot call constant because of import cycle
  def getType: StringID = StringID("SI")

  def asString: String = this.value

  def asProtoStringID: protoStringID = protoStringID.newBuilder().setIDString(this.value).build()

  def toAnyID: AnyID = AnyID.newBuilder().setStringID(this.asProtoStringID).build()

  def getProtoBytes: Array[Byte] = this.asProtoStringID.toByteString.toByteArray
}

object StringID {

  def apply(anyID: protoStringID): StringID = StringID(anyID.getIDString)
}
