package models.common

import models.Abstract.AnyPropertyImpl
import play.api.libs.json.{Json, OWrites, Reads}

case class AnyProperty(impl: AnyPropertyImpl)

object AnyProperty {
  implicit val reads: Reads[AnyProperty] = Json.reads[AnyProperty]

  implicit val writes: OWrites[AnyProperty] = Json.writes[AnyProperty]
}
