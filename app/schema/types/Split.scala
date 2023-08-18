package schema.types

import com.assetmantle.schema.types.base.{Split => protoSplit}

case class Split(value: BigInt) {

  def subtract(out: BigInt): Split = this.copy(value = this.value - out)

  def add(in: BigInt): Split = this.copy(value = this.value + in)

  def canSend(out: BigInt): Boolean = this.value >= out

  def asProtoSplit: protoSplit = protoSplit.newBuilder().setValue(value.toString()).build()

  def getProtoBytes: Array[Byte] = this.asProtoSplit.toByteString.toByteArray

}

object Split {

  def apply(split: protoSplit): Split = Split(value = BigInt(split.getValue))

  def apply(protoBytes: Array[Byte]): Split = Split(protoSplit.parseFrom(protoBytes))

}
