package models.Abstract

import models.common.FeeGrant.{AllowedMsgAllowance, BasicAllowance, PeriodicAllowance}
import models.common.Serializable.Coin
import play.api.libs.json.{Json, Writes}

object FeeGrant {
  abstract class FeeAllowance {
    def getExpiration: Option[String]

    def validate(blockTime: String, fees: Seq[Coin]): (Boolean, FeeAllowance)
  }

  implicit val feeAllowanceWrites: Writes[FeeAllowance] = {
    case basicAllowance: BasicAllowance => Json.toJson(basicAllowance)
    case periodicAllowance: PeriodicAllowance => Json.toJson(periodicAllowance)
    case allowedMsgAllowance: AllowedMsgAllowance => Json.toJson(allowedMsgAllowance)
  }

}