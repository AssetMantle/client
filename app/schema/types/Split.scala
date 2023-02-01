package schema.types

import com.types.{Split => protoSplit}
import schema.id.OwnableID
import schema.id.base.IdentityID

case class Split(ownerID: IdentityID, ownableID: OwnableID, value: BigDecimal) {

  def send(out: BigDecimal): Split = this.copy(value = this.value - out)

  def receive(in: BigDecimal): Split = this.copy(value = this.value + in)

  def canSend(out: BigDecimal): Boolean = this.value >= out

  def asProtoSplit: protoSplit = protoSplit.newBuilder().setOwnerID(ownerID.asProtoIdentityID).setOwnableID(ownableID.toAnyOwnableID).setValue(value.toString()).build()

  def getProtoBytes: Array[Byte] = this.asProtoSplit.toByteString.toByteArray

}

object Split {

  def apply(split: protoSplit): Split = Split(ownerID = IdentityID(split.getOwnerID), ownableID = OwnableID(split.getOwnableID), value = BigDecimal(split.getValue))

  def apply(protoBytes: Array[Byte]): Split = Split(protoSplit.parseFrom(protoBytes))

}
