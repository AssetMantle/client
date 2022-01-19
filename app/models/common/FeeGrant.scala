package models.common

import models.Abstract.{FeeGrant => AbstarctFeeGrant}
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.functional.syntax.{toAlternativeOps, toFunctionalBuilderOps}
import play.api.libs.json._

object FeeGrant {

  private implicit val module: String = constants.Module.TRANSACTION_MESSAGE_FEE_GRANT

  private implicit val logger: Logger = Logger(this.getClass)

  case class Allowance(allowanceType: String, value: AbstarctFeeGrant.FeeAllowance)

  implicit val allowanceReads: Reads[Allowance] = (
    (JsPath \ "allowanceType").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (allowanceApply _)

  implicit val allowanceWrites: Writes[Allowance] = Json.writes[Allowance]

  case class BasicAllowance(spendLimit: Seq[Coin], expiration: Option[String]) extends AbstarctFeeGrant.FeeAllowance

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  implicit val basicAllowanceWrites: Writes[BasicAllowance] = Json.writes[BasicAllowance]

  case class PeriodicAllowance(basicAllowance: BasicAllowance, period: String, periodSpendLimit: Seq[Coin], periodCanSpend: Seq[Coin], periodReset: String) extends AbstarctFeeGrant.FeeAllowance

  implicit val periodicAllowanceReads: Reads[PeriodicAllowance] = Json.reads[PeriodicAllowance]

  implicit val periodicAllowanceWrites: Writes[PeriodicAllowance] = Json.writes[PeriodicAllowance]

  case class AllowedMsgAllowance(allowance: Allowance, allowedMessages: Seq[String]) extends AbstarctFeeGrant.FeeAllowance

  implicit val allowedMsgAllowanceReads: Reads[AllowedMsgAllowance] = Json.reads[AllowedMsgAllowance]

  implicit val allowedMsgAllowanceWrites: Writes[AllowedMsgAllowance] = Json.writes[AllowedMsgAllowance]

  def allowanceApply(allowanceType: String, value: JsObject): Allowance = {
    allowanceType match {
      case constants.Blockchain.FeeGrant.BASIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[BasicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[PeriodicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[AllowedMsgAllowance](value.toString))
    }
  }
}
