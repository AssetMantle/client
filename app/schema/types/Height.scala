package schema.types

import com.assetmantle.schema.types.base.{Height => protoHeight}

import java.nio.{ByteBuffer, ByteOrder}

case class Height(value: Long) {

  require(value >= -1, "INVALID_VALUE_FOR_HEIGHT")

  def AsString: String = this.value.toString

  def getBytes: Array[Byte] = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(this.value).array()

  def asProtoHeight: protoHeight = protoHeight.newBuilder().setValue(this.value).build()

  def getProtoBytes: Array[Byte] = this.asProtoHeight.toByteString.toByteArray

  def compare(that: Height): Int = {
    val difference = this.value - that.value
    if (difference == 0) 0
    else if (difference > 0) 1
    else -1
  }

}

object Height {

  def apply(value: protoHeight): Height = Height(value.getValue)

  def apply(protoBytes: Array[Byte]): Height = Height(protoHeight.parseFrom(protoBytes))

}