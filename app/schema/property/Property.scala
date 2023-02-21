package schema.property

import com.properties.AnyProperty
import org.slf4j.{Logger, LoggerFactory}
import schema.id.base.{DataID, PropertyID, StringID}
import schema.property.base.{MesaProperty, MetaProperty}

abstract class Property {

  def getID: PropertyID

  def getBondedWeight: Int

  def getDataID: DataID

  def getKey: StringID

  def getType: StringID

  def isMeta: Boolean

  def toAnyProperty: AnyProperty

  def getProtoBytes: Array[Byte]

}

object Property {

  private implicit val module: String = constants.Module.SCHEMA_PROPERTY

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(anyProperty: AnyProperty): Property = anyProperty.getImplCase.getNumber match {
    case 1 => MesaProperty(anyProperty.getMesaProperty)
    case 2 => MetaProperty(anyProperty.getMetaProperty)
    case _ => throw new IllegalArgumentException("INVALID_PROPERTY_TYPE")
  }
}
