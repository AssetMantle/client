package models.common

import models.Abstract.AnyPropertyImpl
import play.api.libs.json.{Json, OWrites, Reads}

object BaseProperty {

  case class MetaProperty(id: BaseID.PropertyID, anyData: AnyData) extends AnyPropertyImpl

  implicit val metaPropertyReads: Reads[MetaProperty] = Json.reads[MetaProperty]

  implicit val metaPropertyWrites: OWrites[MetaProperty] = Json.writes[MetaProperty]

  case class MesaProperty(id: BaseID.PropertyID, dataID: BaseID.DataID) extends AnyPropertyImpl

  implicit val mesaPropertyReads: Reads[MesaProperty] = Json.reads[MesaProperty]

  implicit val mesaPropertyWrites: OWrites[MesaProperty] = Json.writes[MesaProperty]

}
