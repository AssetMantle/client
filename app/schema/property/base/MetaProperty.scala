package schema.property.base

import com.assetmantle.schema.data.base.AnyData
import com.assetmantle.schema.properties.base.{AnyProperty, MetaProperty => protoMetaProperty}
import schema.data.Data
import schema.id.base.{DataID, PropertyID, StringID}
import schema.property.Property

case class MetaProperty(id: PropertyID, data: Data) extends Property {

  def getID: PropertyID = this.id

  def getBondedWeight: Int = this.getData.getBondWeight

  def getData: Data = this.data

  def getAnyData: AnyData = this.data.toAnyData

  def getDataID: DataID = this.getData.getDataID

  def getKey: StringID = this.id.keyID

  def getType: StringID = this.getData.getType

  def isMeta: Boolean = true

  def asProtoMetaProperty: protoMetaProperty = protoMetaProperty.newBuilder().setID(this.id.asProtoPropertyID).setData(this.data.toAnyData).build()

  def toAnyProperty: AnyProperty = AnyProperty.newBuilder().setMetaProperty(this.asProtoMetaProperty).build()

  def scrub(): MesaProperty = MesaProperty(id = this.id, dataID = this.getDataID)

  def getProtoBytes: Array[Byte] = this.asProtoMetaProperty.toByteString.toByteArray
}

object MetaProperty {

  def apply(value: protoMetaProperty): MetaProperty = MetaProperty(id = PropertyID(value.getID), data = Data(value.getData))

  def apply(protoBytes: Array[Byte]): MetaProperty = MetaProperty(protoMetaProperty.parseFrom(protoBytes))

}