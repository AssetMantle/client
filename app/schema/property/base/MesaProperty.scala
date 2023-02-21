package schema.property.base

import com.properties.{AnyProperty, MesaProperty => protoMesaProperty}
import schema.id.base.{DataID, PropertyID, StringID}
import schema.property.Property

case class MesaProperty(id: PropertyID, dataID: DataID) extends Property {

  def getID: PropertyID = this.id

  def getDataID: DataID = this.dataID

  def getBondedWeight: Int = constants.Data.getBondedWeight(this.getType)

  def getKey: StringID = this.id.keyID

  def getType: StringID = this.dataID.getTypeID

  def isMeta: Boolean = false

  def asProtoMesaProperty: protoMesaProperty = protoMesaProperty.newBuilder().setID(this.id.asProtoPropertyID).setDataID(this.dataID.asProtoDataID).build()

  def toAnyProperty: AnyProperty = AnyProperty.newBuilder().setMesaProperty(this.asProtoMesaProperty).build()

  def getProtoBytes: Array[Byte] = this.asProtoMesaProperty.toByteString.toByteArray
}

object MesaProperty {

  def apply(value: protoMesaProperty): MesaProperty = MesaProperty(id = PropertyID(value.getID), dataID = schema.id.base.DataID(value.getDataID))

  def apply(protoBytes: Array[Byte]): MesaProperty = MesaProperty(protoMesaProperty.parseFrom(protoBytes))

}