package schema.id.base

import com.ids.{AnyID, StringID => protoStringID}
import schema.id.ID

case class StringID(value: String) extends ID {

  def getBytes: Array[Byte] = this.value.getBytes

  def asString: String = this.value

  def asProtoStringID: protoStringID = protoStringID.newBuilder().setIDString(this.value).build()

  def toAnyID: AnyID = AnyID.newBuilder().setStringID(this.asProtoStringID).build()

  def getProtoBytes: Array[Byte] = this.asProtoStringID.toByteString.toByteArray
}

object StringID {

  def apply(anyID: protoStringID): StringID = StringID(anyID.getIDString)
}
