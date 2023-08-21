package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, ListData => protoListData}
import schema.data._
import schema.id.base.{DataID, HashID, StringID}
import schema.utilities.common.byteArraysCompare

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

case class ListData(value: Seq[ListableData]) extends Data {

  require(value.length <= schema.data.constants.MaxListLength, "LIST_DATA_SIZE_EXCEEDED")
  require(this.value.map(_.getType.value).forall(_ == this.value.head.getType.value), "LIST_DATA_DOES_NOT_CONTAINS_SAME_ELEMENT_TYPE")

  def getType: StringID = constants.ListDataTypeID

  def getBondWeight: Int = constants.ListDataWeight

  def getAnyDataList: Seq[AnyData] = this.value.map(_.toAnyData)

  def getDataList: Seq[Data] = this.value

  def getListableDataList: Seq[ListableData] = this.value

  def zeroValue: Data = ListData(Seq())

  def sort: ListData = ListData(this.value.sortWith((x, y) => byteArraysCompare(x.getBytes, y.getBytes) < 0))

  def search(dataID: DataID): Int = this.getDataList.indexWhere(_.getDataID.getBytes.sameElements(dataID.getBytes))

  def generateHashID: HashID = if (this.value.isEmpty) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoListData: protoListData = protoListData.newBuilder().addAllValue(this.value.map(_.toAnyListableData).asJava).build()

  def toAnyData: AnyData = AnyData.newBuilder().setListData(this.asProtoListData).build()

  def getBytes: Array[Byte] = {
    val sortedBytes = this.sort.getDataList.map(_.getBytes)
    val outputStream = new ByteArrayOutputStream(sortedBytes.toArray.flatten.length + ((sortedBytes.length - 1) * constants.ListBytesSeparator.getBytes.length))
    sortedBytes.foreach(x => {
      outputStream.writeBytes(x)
      outputStream.writeBytes(constants.ListBytesSeparator.getBytes)
    })
    outputStream.toByteArray.dropRight(constants.ListBytesSeparator.getBytes.length)
  }

  def getProtoBytes: Array[Byte] = this.asProtoListData.toByteString.toByteArray

  def viewString: String = "List: " + this.value.map(_.viewString).mkString(", ")

  def isListableData: Boolean = false

  def add(addDataList: Seq[ListableData]): ListData = {
    var updatedList = this.sort.value
    val elementType = if (updatedList.nonEmpty) updatedList.head.getType else if (addDataList.nonEmpty) addDataList.head.getType else schema.id.base.StringID("")
    addDataList.filter(_.getType.compare(elementType) == 0).foreach(x => {
      val index = this.value.indexWhere(_.getDataID.getBytes.sameElements(x.getDataID.getBytes))
      if (index == -1) updatedList = updatedList :+ x
    })
    ListData(updatedList).sort
  }

  def add(data: ListableData): ListData = this.add(Seq(data))

  def remove(removeDataList: Seq[ListableData]): ListData = {
    var updatedList = this.sort.value
    removeDataList.foreach(x => {
      val index = this.value.indexWhere(_.getDataID.getBytes.sameElements(x.getDataID.getBytes))
      if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    })
    ListData(updatedList)
  }

  def remove(data: ListableData): ListData = this.remove(Seq(data))
}

object ListData {

  def apply(listValue: protoListData): ListData = ListData(listValue.getValueList.asScala.toSeq.map(x => ListableData(x)))

  def apply(protoBytes: Array[Byte]): ListData = ListData(protoListData.parseFrom(protoBytes))

}
