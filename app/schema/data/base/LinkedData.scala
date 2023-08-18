package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, AnyListableData, LinkedData => protoLinkedData}
import schema.data._
import schema.id.base.{HashID, StringID}

import java.io.ByteArrayOutputStream

case class LinkedData(resourceID: HashID, extensionID: StringID, serviceEndpoint: String) extends ListableData {

  def getType: StringID = constants.LinkedDataTypeID

  def getBondWeight: Int = constants.LinkedDataWeight

  def compare(listableData: ListableData): Int = schema.utilities.common.byteArraysCompare(this.getBytes, listableData.asInstanceOf[LinkedData].getBytes)

  def zeroValue: Data = LinkedData(resourceID = schema.utilities.ID.generateHashID(), extensionID = StringID(""), serviceEndpoint = "")

  def getBytes: Array[Byte] = {
    val outputStream = new ByteArrayOutputStream(this.resourceID.getBytes.length + this.extensionID.getBytes.length + this.serviceEndpoint.getBytes.length + 2 * constants.ListBytesSeparator.getBytes.length)
    outputStream.writeBytes(this.resourceID.getBytes)
    outputStream.writeBytes(constants.ListBytesSeparator.getBytes)
    outputStream.writeBytes(this.extensionID.getBytes)
    outputStream.writeBytes(constants.ListBytesSeparator.getBytes)
    outputStream.writeBytes(this.serviceEndpoint.getBytes)
    outputStream.toByteArray
  }

  def generateHashID: HashID = if (this.compare(this.zeroValue.asInstanceOf[ListableData]) == 0) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoLinkedData: protoLinkedData = protoLinkedData.newBuilder()
    .setResourceID(this.resourceID.asProtoHashID)
    .setExtensionID(this.extensionID.asProtoStringID)
    .setServiceEndpoint(this.serviceEndpoint)
    .build()

  def toAnyData: AnyData = AnyData.newBuilder().setLinkedData(this.asProtoLinkedData).build()

  def toAnyListableData: AnyListableData = AnyListableData.newBuilder().setLinkedData(this.asProtoLinkedData).build()

  def getProtoBytes: Array[Byte] = this.asProtoLinkedData.toByteString.toByteArray

  def viewString: String = "Linked: " + this.resourceID.asString + ", " + this.extensionID.asString + "," + this.serviceEndpoint
}

object LinkedData {

  def apply(value: protoLinkedData): LinkedData = LinkedData(resourceID = HashID(value.getResourceID), extensionID = StringID(value.getExtensionID), serviceEndpoint = value.getServiceEndpoint)

  def apply(protoBytes: Array[Byte]): LinkedData = LinkedData(protoLinkedData.parseFrom(protoBytes))
}
