package models.Abstract

import models.common.FeeGrant.{AllowedMsgAllowance, BasicAllowance, PeriodicAllowance}
import models.common.Coin
import play.api.libs.json.{Json, Writes}
import utilities.Date.RFC3339

object FeeGrant {
  abstract class FeeAllowance {
    def getExpiration: Option[RFC3339]

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, FeeAllowance)
  }

  implicit val feeAllowanceWrites: Writes[FeeAllowance] = {
    case basicAllowance: BasicAllowance => Json.toJson(basicAllowance)
    case periodicAllowance: PeriodicAllowance => Json.toJson(periodicAllowance)
    case allowedMsgAllowance: AllowedMsgAllowance => Json.toJson(allowedMsgAllowance)
  }

}