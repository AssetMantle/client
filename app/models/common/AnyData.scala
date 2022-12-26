package models.common

import models.Abstract.AnyDataImpl
import play.api.libs.json.{Json, OWrites, Reads}

case class AnyData(impl: AnyDataImpl)

object AnyData {
  implicit val anyDataReads: Reads[AnyData] = Json.reads[AnyData]

  implicit val anyDataWrites: OWrites[AnyData] = Json.writes[AnyData]
}
