package schema.qualified

import com.lists.{PropertyList => protoPropertyList}
import com.qualified.{Mutables => protoMutables}
import schema.id.base.PropertyID
import schema.list.PropertyList
import schema.property.Property

case class Mutables(propertyList: PropertyList) {

  def getProperty(id: PropertyID): Option[Property] = this.propertyList.getProperty(id)

  def getProperties: Seq[Property] = this.propertyList.getProperties

  def getProtoPropertyList: protoPropertyList = this.propertyList.asProtoPropertyList

  def getPropertyIDList: Seq[PropertyID] = this.getProperties.map(_.getID)

  def asProtoMutables: protoMutables = protoMutables.newBuilder().setPropertyList(this.getProtoPropertyList).build()

  def getProtoBytes: Array[Byte] = this.asProtoMutables.toByteString.toByteArray

  def add(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.add(properties))

  def remove(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.remove(properties))

  def mutate(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.mutate(properties))
}


object Mutables {

  def apply(mutables: protoMutables): Mutables = Mutables(PropertyList(mutables.getPropertyList))

  def apply(protoBytes: Array[Byte]): Mutables = Mutables(protoMutables.parseFrom(protoBytes))

}