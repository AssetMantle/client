package schema.list

import com.ids.AnyID
import com.lists.{IDList => protoIDList}
import schema.id.ID

import scala.jdk.CollectionConverters._

case class IDList(idList: Seq[ID]) {

  def getIDs: Seq[ID] = this.idList

  def getAnyIDs: Seq[AnyID] = this.getIDs.map(_.toAnyID)

  def asProtoIDList: protoIDList = protoIDList.newBuilder().addAllIDList(this.idList.map(_.toAnyID).asJava).build()

  def getProtoBytes: Array[Byte] = this.asProtoIDList.toByteString.toByteArray

}

object IDList {

  def apply(idList: protoIDList): IDList = IDList(idList.getIDListList.asScala.toSeq.map(x => ID(x)))

  def apply(protoBytes: Array[Byte]): IDList = IDList(protoIDList.parseFrom(protoBytes))

}
