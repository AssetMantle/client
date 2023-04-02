package schema.list

import com.lists.{PropertyList => protoPropertyList}
import com.properties.AnyProperty
import schema.id.base.PropertyID
import schema.property.Property
import schema.property.base.MetaProperty

import scala.jdk.CollectionConverters._

case class PropertyList(propertyList: Seq[Property]) {

  def getProperties: Seq[Property] = this.propertyList

  def getAnyProperties: Seq[AnyProperty] = this.getProperties.map(_.toAnyProperty)

  def getProperty(id: PropertyID): Option[Property] = this.getProperties.find(x => x.getID.asString == id.asString)

  def getPropertyIDList: IDList = IDList(this.propertyList.map(_.getID))

  def getPropertyAsAnyProperty(id: PropertyID): Option[AnyProperty] = this.getProperty(id).map(_.toAnyProperty)

  def asProtoPropertyList: protoPropertyList = protoPropertyList.newBuilder().addAllPropertyList(this.propertyList.map(_.toAnyProperty).asJava).build()

  def scrub: PropertyList = PropertyList(this.getProperties.map { property =>
    if (property.isMeta) property.asInstanceOf[MetaProperty].scrub() else property
  })

  def getProtoBytes: Array[Byte] = this.asProtoPropertyList.toByteString.toByteArray

  def add(properties: Seq[Property]): PropertyList = {
    var updatedList = this.propertyList
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.propertyList.indexWhere(_.getID.getBytes.sameElements(xBytes))
      updatedList = if (index == -1) updatedList :+ x else updatedList.updated(index, x)
    })
    new PropertyList(propertyList = updatedList)
  }

  def remove(properties: Seq[Property]): PropertyList = {
    var updatedList = this.propertyList
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.propertyList.indexWhere(_.getID.getBytes.sameElements(xBytes))
      if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    })
    new PropertyList(propertyList = updatedList)
  }

  def mutate(properties: Seq[Property]): PropertyList = {
    var updatedList = this.propertyList
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.propertyList.indexWhere(_.getID.getBytes.sameElements(xBytes))
      if (index != -1) updatedList = updatedList.updated(index, x)
    })
    new PropertyList(updatedList)
  }
}

object PropertyList {

  def apply(properties: protoPropertyList): PropertyList = PropertyList(properties.getPropertyListList.asScala.toSeq.map(x => Property(x)))

  def apply(protoBytes: Array[Byte]): PropertyList = PropertyList(protoPropertyList.parseFrom(protoBytes))

}
