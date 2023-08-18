package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, OrderID => protoOrderID}
import schema.id._

case class OrderID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def getType: StringID = constants.OrderIDType

  def asString: String = schema.utilities.common.base64URLEncoder(this.getBytes)

  def asProtoOrderID: protoOrderID = protoOrderID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setOrderID(this.asProtoOrderID).build()

  def getProtoBytes: Array[Byte] = this.asProtoOrderID.toByteString.toByteArray

  def compare(id: ID): Int = schema.utilities.common.byteArraysCompare(this.getBytes, id.asInstanceOf[OrderID].getBytes)

}

object OrderID {
  def apply(anyID: protoOrderID): OrderID = OrderID(HashID(anyID.getHashID))

  def apply(value: Array[Byte]): OrderID = OrderID(HashID(value))

  def apply(value: String): OrderID = OrderID(HashID(utilities.Secrets.base64URLDecode(value)))
}