package schema.id.base

import com.ids.{AnyID, OrderID => protoOrderID}
import schema.id.ID

case class OrderID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def asString: String = utilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoOrderID: protoOrderID = protoOrderID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setOrderID(this.asProtoOrderID).build()

  def getProtoBytes: Array[Byte] = this.asProtoOrderID.toByteString.toByteArray

}

object OrderID {
  def apply(anyID: protoOrderID): OrderID = OrderID(HashID(anyID.getHashID))
}