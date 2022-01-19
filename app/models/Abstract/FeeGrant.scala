package models.Abstract

import models.common.FeeGrant.{AllowedMsgAllowance, BasicAllowance, PeriodicAllowance}
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}

object FeeGrant {
  abstract class FeeAllowance {}

  implicit val feeAllowanceWrites: Writes[FeeAllowance] = {
    case basicAllowance: BasicAllowance => Json.toJson(basicAllowance)
    case periodicAllowance: PeriodicAllowance => Json.toJson(periodicAllowance)
    case allowedMsgAllowance: AllowedMsgAllowance => Json.toJson(allowedMsgAllowance)
  }

}