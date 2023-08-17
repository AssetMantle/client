package schema.property

import com.assetmantle.schema.properties.base.AnyProperty
import schema.data.Data
import schema.id.base.{DataID, PropertyID, StringID}
import schema.property.base.{MesaProperty, MetaProperty}

abstract class Property {

  def getID: PropertyID

  def getBondedWeight: Int

  def getDataID: DataID

  def getKey: StringID

  def getType: StringID

  def isMeta: Boolean

  def isMesa: Boolean

  def toAnyProperty: AnyProperty

  def getProtoBytes: Array[Byte]

  def scrub(): MesaProperty

  def mutate(data: Data): Property

}

object Property {
  def apply(anyProperty: AnyProperty): Property = anyProperty.getImplCase.getNumber match {
    case 1 => MesaProperty(anyProperty.getMesaProperty)
    case 2 => MetaProperty(anyProperty.getMetaProperty)
    case _ => throw new IllegalArgumentException("INVALID_PROPERTY_IMPL_CASE_NUMBER: " + anyProperty.getImplCase.getNumber.toString)
  }

  def apply(protoBytes: Array[Byte]): Property = Property(AnyProperty.parseFrom(protoBytes))
}
