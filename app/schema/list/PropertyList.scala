package schema.list

import com.lists.{PropertyList => protoPropertyList}
import com.properties.AnyProperty
import schema.id.base.PropertyID
import schema.property.Property
import utilities.ID.byteArraysCompare

import scala.jdk.CollectionConverters._

case class PropertyList(properties: Seq[Property]) {

  def getProperties: Seq[Property] = this.properties

  def getAnyProperties: Seq[AnyProperty] = this.getProperties.map(_.toAnyProperty)

  def getTotalBondWeight: Int = this.properties.map(_.getBondedWeight).sum

  def getProperty(id: PropertyID): Option[Property] = this.properties.find(x => x.getID.getBytes.sameElements(id.getBytes))

  def getPropertyIDList: IDList = IDList(this.properties.map(_.getID))

  def asProtoPropertyList: protoPropertyList = protoPropertyList.newBuilder().addAllProperties(this.properties.map(_.toAnyProperty).asJava).build()

  def scrub: PropertyList = PropertyList(this.properties.map(_.scrub()))

  def getProtoBytes: Array[Byte] = this.asProtoPropertyList.toByteString.toByteArray

  def add(properties: Seq[Property]): PropertyList = {
    var updatedList = this.properties
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.properties.indexWhere(_.getID.getBytes.sameElements(xBytes))
      updatedList = if (index == -1) updatedList :+ x else updatedList.updated(index, x)
    })
    new PropertyList(properties = updatedList)
  }

  def remove(properties: Seq[Property]): PropertyList = {
    var updatedList = this.properties
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.properties.indexWhere(_.getID.getBytes.sameElements(xBytes))
      if (index != -1) updatedList = updatedList.zipWithIndex.filter(_._2 != index).map(_._1)
    })
    new PropertyList(properties = updatedList)
  }

  def mutate(properties: Seq[Property]): PropertyList = {
    var updatedList = this.properties
    properties.foreach(x => {
      val xBytes = x.getID.getBytes
      val index = this.properties.indexWhere(_.getID.getBytes.sameElements(xBytes))
      if (index != -1) updatedList = updatedList.updated(index, x)
    })
    new PropertyList(updatedList)
  }

  def sort: PropertyList = PropertyList(this.properties.sortWith((x, y) => byteArraysCompare(x.getID.getBytes, y.getID.getBytes) < 0))
}

object PropertyList {

  def apply(properties: protoPropertyList): PropertyList = PropertyList(properties.getPropertiesList.asScala.toSeq.map(x => Property(x)))

  def apply(protoBytes: Array[Byte]): PropertyList = PropertyList(protoPropertyList.parseFrom(protoBytes))

  //  def apply(properties: Seq[AnyProperty]): PropertyList = PropertyList(properties.map(x => Property(x)))

}
