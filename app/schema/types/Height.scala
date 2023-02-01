package schema.types

import com.types.{Height => protoHeight}

import java.nio.{ByteBuffer, ByteOrder}

case class Height(value: Long) {

  def AsString: String = this.value.toString

  def getBytes: Array[Byte] = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(this.value).array()

  def asProtoHeight: protoHeight = protoHeight.newBuilder().setValue(this.value).build()

  def getProtoBytes: Array[Byte] = this.asProtoHeight.toByteString.toByteArray

}

object Height {

  def apply(value: protoHeight): Height = Height(value.getValue)

  def apply(protoBytes: Array[Byte]): Height = Height(protoHeight.parseFrom(protoBytes))

}