package models.common

import models.Abstract.AnyIDImpl
import play.api.libs.json.{Json, OWrites, Reads}

case class AnyID(impl: AnyIDImpl)

object AnyID {
  implicit val reads: Reads[AnyID] = Json.reads[AnyID]

  implicit val writes: OWrites[AnyID] = Json.writes[AnyID]
}
