package schema.data.base

import com.assetmantle.schema.data.base.{AnyData, ListData => protoListData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}
import schema.utilities.ID.byteArraysCompare

import scala.jdk.CollectionConverters._

case class ListData(dataList: Seq[Data]) extends Data {
  def getType: StringID = constants.Data.ListDataTypeID

  def getBondWeight: Int = constants.Data.ListDataWeight

  def getDataID: DataID = DataID(typeID = constants.Data.ListDataTypeID, hashID = this.generateHashID)

  def getAnyDataList: Seq[AnyData] = this.dataList.map(_.toAnyData)

  def getDataList: Seq[Data] = this.dataList

  def zeroValue: Data = ListData(dataList = Seq[Data]())

  def sort: ListData = ListData(this.getDataList.sortWith((x, y) => byteArraysCompare(x.getBytes, y.getBytes) < 0))

  def search(dataID: DataID): Int = this.getDataList.indexWhere(_.getDataID.getBytes.sameElements(dataID.getBytes))

  def generateHashID: HashID = if (this.dataList.isEmpty) schema.utilities.ID.generateHashID() else schema.utilities.ID.generateHashID(this.getBytes)

  def asProtoListData: protoListData = protoListData.newBuilder().addAllDataList(this.getAnyDataList.asJava).build()

  def toAnyData: AnyData = AnyData.newBuilder().setListData(this.asProtoListData).build()

  def getBytes: Array[Byte] = this.sort.getDataList.map(_.getBytes).toArray.flatten

  def getProtoBytes: Array[Byte] = this.asProtoListData.toByteString.toByteArray

  def viewString: String = this.dataList.map(_.viewString).mkString(", ")

  def add(addData: Data): ListData = {
    var updatedList = this.dataList
    val xBytes = addData.getDataID.getBytes
    val index = this.dataList.indexWhere(_.getDataID.getBytes.sameElements(xBytes))
    if (index == -1) updatedList = updatedList :+ addData
    new ListData(updatedList)
  }

  def remove(removeData: Data): ListData = {
    var updatedList = this.dataList
    val xBytes = removeData.getDataID.getBytes
    val index = this.dataList.indexWhere(_.getDataID.getBytes.sameElements(xBytes))
    if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    new ListData(updatedList)
  }
}

object ListData {

  def apply(value: protoListData): ListData = ListData(value.getDataListList.asScala.toSeq.map(x => Data(x)))

  def apply(protoBytes: Array[Byte]): ListData = ListData(protoListData.parseFrom(protoBytes))

  //  def apply(dataList: Seq[AnyData]): ListData = ListData(dataList.map(x => Data(x)))

}
