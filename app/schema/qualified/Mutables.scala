package schema.qualified

import com.assetmantle.schema.lists.base.{PropertyList => protoPropertyList}
import com.assetmantle.schema.qualified.base.{Mutables => protoMutables}
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

  def getTotalBondWeight: Int = this.propertyList.getTotalBondWeight

  def add(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.add(properties))

  def remove(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.remove(properties))

  def mutate(properties: Seq[Property]): Mutables = new Mutables(this.propertyList.mutate(properties))
}


object Mutables {

  def apply(mutables: protoMutables): Mutables = Mutables(PropertyList(mutables.getPropertyList))

  def apply(protoBytes: Array[Byte]): Mutables = Mutables(protoMutables.parseFrom(protoBytes))

  def apply(properties: Seq[Property]): Mutables = Mutables(PropertyList(properties))
}