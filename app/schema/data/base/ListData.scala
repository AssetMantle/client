package schema.data.base

import com.data.{AnyData, ListData => protoListData}
import schema.data.Data
import schema.id.base.{DataID, HashID, StringID}

import scala.jdk.CollectionConverters._


case class ListData(dataList: Seq[AnyData]) extends Data {
  def getType: StringID = constants.Data.ListDataTypeID

  def getBondWeight: Int = constants.Data.ListDataWeight

  def getDataID: DataID = DataID(typeID = constants.Data.ListDataTypeID, hashID = this.generateHashID)

  def getAnyDataList: Seq[AnyData] = this.dataList

  def zeroValue: Data = ListData(dataList = Seq())

  def generateHashID: HashID = if (this.dataList.isEmpty) utilities.ID.generateHashID() else utilities.ID.generateHashID(this.getBytes)

  def asProtoListData: protoListData = protoListData.newBuilder().addAllDataList(this.dataList.asJava).build()

  def toAnyData: AnyData = AnyData.newBuilder().setListData(this.asProtoListData).build()

  def getBytes: Array[Byte] = this.dataList.map(x => Data(x).getBytes).toArray.flatten

  def getProtoBytes: Array[Byte] = this.asProtoListData.toByteString.toByteArray

  override def viewString: String = this.dataList.map(x => Data(x).viewString).mkString(", ")
}

object ListData {

  def apply(value: protoListData): ListData = ListData(value.getDataListList.asScala.toSeq)

  def apply(protoBytes: Array[Byte]): ListData = ListData(protoListData.parseFrom(protoBytes))

}
