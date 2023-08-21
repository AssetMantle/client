package schema.list

import com.assetmantle.schema.ids.base.AnyID
import com.assetmantle.schema.lists.base.{IDList => protoIDList}
import schema.data.base.{IDData, ListData}
import schema.id.ID
import schema.utilities.common.byteArraysCompare

import scala.jdk.CollectionConverters._

case class IDList(idList: Seq[ID]) {

  require(this.idList.map(_.getType.value).forall(_ == this.idList.head.getType.value), "ID_LIST_DOES_NOT_CONTAINS_SAME_ELEMENT_TYPE")

  def getIDs: Seq[ID] = this.idList

  def getAnyIDs: Seq[AnyID] = this.getIDs.map(_.toAnyID)

  def asProtoIDList: protoIDList = protoIDList.newBuilder().addAllAnyIDs(this.idList.map(_.toAnyID).asJava).build()

  def getProtoBytes: Array[Byte] = this.asProtoIDList.toByteString.toByteArray

  def add(addIDs: Seq[ID]): IDList = {
    var updatedList = this.sort.idList
    val elementType = if (updatedList.nonEmpty) updatedList.head.getType else if (addIDs.nonEmpty) addIDs.head.getType else schema.id.base.StringID("")
    addIDs.filter(_.getType.compare(elementType) == 0).foreach(x => {
      val index = updatedList.indexWhere(_.getBytes.sameElements(x.getBytes))
      if (index == -1) updatedList = updatedList :+ x
    })
    IDList(idList = updatedList).sort
  }

  def add(id: ID): IDList = this.add(Seq(id))

  def remove(ids: Seq[ID]): IDList = {
    var updatedList = this.sort.idList
    ids.foreach(x => {
      val index = this.idList.indexWhere(_.getBytes.sameElements(x.getBytes))
      if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    })
    IDList(idList = updatedList)
  }

  def remove(id: ID): IDList = this.remove(Seq(id))

  def sort: IDList = IDList(this.idList.sortWith((x, y) => byteArraysCompare(x.getBytes, y.getBytes) < 0))

  def toListData: ListData = ListData(this.sort.idList.map(x => IDData(x)))
}

object IDList {

  def apply(idList: protoIDList): IDList = IDList(idList.getAnyIDsList.asScala.toSeq.map(x => ID(x)))

  def apply(protoBytes: Array[Byte]): IDList = IDList(protoIDList.parseFrom(protoBytes))

}
