package schema.qualified

import com.assetmantle.schema.lists.base.{PropertyList => protoPropertyList}
import com.assetmantle.schema.qualified.base.{Immutables => protoImmutables}
import schema.id.base.{HashID, PropertyID}
import schema.list.PropertyList
import schema.property.Property

case class Immutables(propertyList: PropertyList) {

  def getProperty(id: PropertyID): Option[Property] = this.propertyList.getProperty(id)

  def getProperties: Seq[Property] = this.propertyList.getProperties

  def getProtoPropertyList: protoPropertyList = this.propertyList.asProtoPropertyList

  def getPropertyIDList: Seq[PropertyID] = this.getProperties.map(_.getID)

  def getTotalBondWeight: Int = this.propertyList.getTotalBondWeight

  def generateHashID: HashID = schema.utilities.ID.generateHashIDFromList(this.getProperties.map(_.getDataID.getHashID.getBytes))

  def asProtoImmutables: protoImmutables = protoImmutables.newBuilder().setPropertyList(this.getProtoPropertyList).build()

  def getProtoBytes: Array[Byte] = this.asProtoImmutables.toByteString.toByteArray
}

object Immutables {

  def apply(immutables: protoImmutables): Immutables = Immutables(PropertyList(immutables.getPropertyList))

  def apply(protoBytes: Array[Byte]): Immutables = Immutables(protoImmutables.parseFrom(protoBytes))

}