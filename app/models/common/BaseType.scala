package models.common

import models.common.BaseID._
import play.api.libs.json.{Json, OWrites, Reads}

object BaseType {

  case class Height(value: Int)

  object Height {
    implicit val reads: Reads[Height] = Json.reads[Height]

    implicit val writes: OWrites[Height] = Json.writes[Height]
  }

  case class Split(identityID: IdentityID, ownableID: OwnableID, value: BigDecimal)

  object Split {
    implicit val reads: Reads[Split] = Json.reads[Split]

    implicit val writes: OWrites[Split] = Json.writes[Split]
  }

}
