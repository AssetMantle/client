package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.Data._
import queries.responses.blockchain.common.ID._

object Property {

  case class MesaProperty(data_i_d: DataID, i_d: PropertyID) {

    def toMesaProperty: schema.property.base.MesaProperty = schema.property.base.MesaProperty(id = this.i_d.toPropertyID, dataID = this.data_i_d.toDataID)
  }

  implicit val MesaPropertyReads: Reads[MesaProperty] = Json.reads[MesaProperty]

  case class MetaProperty(data: AnyData, i_d: PropertyID) {
    def toMetaProperty: schema.property.base.MetaProperty = schema.property.base.MetaProperty(id = this.i_d.toPropertyID, data = this.data.toData.toAnyData)
  }

  implicit val MetaPropertyReads: Reads[MetaProperty] = Json.reads[MetaProperty]

  case class AnyProperty(mesa_property: Option[MesaProperty], meta_property: Option[MetaProperty]) {
    def toProperty: schema.property.Property = {
      if (this.mesa_property.isDefined) this.mesa_property.get.toMesaProperty
      else this.meta_property.get.toMetaProperty
    }
  }

  implicit val AnyPropertyReads: Reads[AnyProperty] = Json.reads[AnyProperty]

}
