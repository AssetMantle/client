package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, ListData => protoListData}
import schema.data._
import schema.id.base.{DataID, HashID, StringID}
import schema.utilities.ID.byteArraysCompare

import java.io.ByteArrayOutputStream
import scala.jdk.CollectionConverters._

case class ListData(listableData: Seq[ListableData]) extends Data {

  def getType: StringID = constants.ListDataTypeID

  def getBondWeight: Int = constants.ListDataWeight

  def getDataID: DataID = DataID(typeID = constants.ListDataTypeID, hashID = this.generateHashID)

  def getAnyDataList: Seq[AnyData] = this.listableData.map(_.toAnyData)

  def getDataList: Seq[Data] = this.listableData

  def getListableDataList: Seq[ListableData] = this.listableData

  def zeroValue: Data = ListData(Seq())

  def sort: ListData = ListData(this.listableData.sortWith((x, y) => byteArraysCompare(x.getBytes, y.getBytes) < 0))

  def search(dataID: DataID): Int = this.getDataList.indexWhere(_.getDataID.getBytes.sameElements(dataID.getBytes))

  def generateHashID: HashID = if (this.listableData.isEmpty) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoListData: protoListData = protoListData.newBuilder().addAllAnyListableData(this.listableData.map(_.toAnyListableData).asJava).build()

  def toAnyData: AnyData = AnyData.newBuilder().setListData(this.asProtoListData).build()

  def getBytes: Array[Byte] = {
    val sortedBytes = this.sort.getDataList.map(_.getBytes)
    val outputStream = new ByteArrayOutputStream(sortedBytes.toArray.flatten.length + ((sortedBytes.length - 1) * constants.ListSeparator.getBytes.length))
    sortedBytes.foreach(x => {
      outputStream.writeBytes(x)
      outputStream.writeBytes(constants.ListSeparator.getBytes)
    })
    outputStream.toByteArray.dropRight(constants.ListSeparator.getBytes.length)
  }

  def getProtoBytes: Array[Byte] = this.asProtoListData.toByteString.toByteArray

  def viewString: String = "List: " + this.listableData.map(_.viewString).mkString(", ")

  def isListableData: Boolean = false

  def add(addData: ListableData): ListData = {
    var updatedList = this.listableData
    val xBytes = addData.getDataID.getBytes
    val index = this.listableData.indexWhere(_.getDataID.getBytes.sameElements(xBytes))
    if (index == -1) updatedList = updatedList :+ addData
    new ListData(updatedList)
  }

  def remove(removeData: ListableData): ListData = {
    var updatedList = this.listableData
    val xBytes = removeData.getDataID.getBytes
    val index = this.listableData.indexWhere(_.getDataID.getBytes.sameElements(xBytes))
    if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    new ListData(updatedList)
  }
}

object ListData {

  def apply(value: protoListData): ListData = ListData(value.getAnyListableDataList.asScala.toSeq.map(x => ListableData(x)))

  def apply(protoBytes: Array[Byte]): ListData = ListData(protoListData.parseFrom(protoBytes))

}
