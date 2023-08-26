package schema.property.base

import com.assetmantle.schema.properties.base.{AnyProperty, MesaProperty => protoMesaProperty}
import schema.data.Data
import schema.id.base.{DataID, PropertyID, StringID}
import schema.property.Property

case class MesaProperty(id: PropertyID, dataID: DataID) extends Property {

  require(this.id.typeID.value == this.dataID.typeID.value, "MESA_PROPERTY_TYPE_ID_AND_DATA_ID_TYPE_MISMATCH")

  def getID: PropertyID = this.id

  def getDataID: DataID = this.dataID

  def getBondedWeight: Int = this.getDataID.getBondWeight

  def getKey: StringID = this.id.keyID

  def getType: StringID = this.dataID.getDataTypeID

  def isMeta: Boolean = false

  def isMesa: Boolean = true

  def asProtoMesaProperty: protoMesaProperty = protoMesaProperty.newBuilder().setID(this.id.asProtoPropertyID).setDataID(this.dataID.asProtoDataID).build()

  def toAnyProperty: AnyProperty = AnyProperty.newBuilder().setMesaProperty(this.asProtoMesaProperty).build()

  def getProtoBytes: Array[Byte] = this.asProtoMesaProperty.toByteString.toByteArray

  def scrub(): MesaProperty = this

  def mutate(data: Data): Property = MesaProperty(id = this.id, dataID = data.getDataID)
}

object MesaProperty {

  def apply(value: protoMesaProperty): MesaProperty = MesaProperty(id = PropertyID(value.getID), dataID = schema.id.base.DataID(value.getDataID))

  def apply(protoBytes: Array[Byte]): MesaProperty = MesaProperty(protoMesaProperty.parseFrom(protoBytes))

  def apply(id: PropertyID, data: Data): MesaProperty = MesaProperty(id, data.getDataID)

  def apply(keyID: StringID, data: Data): MesaProperty = MesaProperty(id = PropertyID(keyID = keyID, typeID = data.getType), data.getDataID)

}