package schema.list

import com.assetmantle.schema.ids.base.AnyID
import com.assetmantle.schema.lists.base.{IDList => protoIDList}
import schema.id.ID
import schema.utilities.ID.byteArraysCompare

import scala.jdk.CollectionConverters._

case class IDList(idList: Seq[ID]) {

  def getIDs: Seq[ID] = this.idList

  def getAnyIDs: Seq[AnyID] = this.getIDs.map(_.toAnyID)

  def asProtoIDList: protoIDList = protoIDList.newBuilder().addAllAnyIDs(this.idList.map(_.toAnyID).asJava).build()

  def getProtoBytes: Array[Byte] = this.asProtoIDList.toByteString.toByteArray

  def add(ids: Seq[ID]): IDList = {
    var updatedList = this.idList
    ids.foreach(x => {
      val xBytes = x.getBytes
      val index = this.idList.indexWhere(_.getBytes.sameElements(xBytes))
      if (index == -1) updatedList = updatedList :+ x
    })
    new IDList(idList = updatedList)
  }

  def remove(ids: Seq[ID]): IDList = {
    var updatedList = this.idList
    ids.foreach(x => {
      val xBytes = x.getBytes
      val index = this.idList.indexWhere(_.getBytes.sameElements(xBytes))
      if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    })
    new IDList(idList = updatedList)
  }

  def sort: IDList = IDList(this.idList.sortWith((x, y) => byteArraysCompare(x.getBytes, y.getBytes) < 0))
}

object IDList {

  def apply(idList: protoIDList): IDList = IDList(idList.getAnyIDsList.asScala.toSeq.map(x => ID(x)))

  def apply(protoBytes: Array[Byte]): IDList = IDList(protoIDList.parseFrom(protoBytes))

}
