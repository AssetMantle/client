package models.common

import models.Abstract.{FeeGrant => AbstarctFeeGrant}
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import utilities.Blockchain.{FeeGrant => utilitiesFeeGrant}
import utilities.Date.RFC3339

object FeeGrant {

  private implicit val module: String = constants.Module.TRANSACTION_MESSAGE_FEE_GRANT

  private implicit val logger: Logger = Logger(this.getClass)

  case class Allowance(allowanceType: String, value: AbstarctFeeGrant.FeeAllowance) {
    def validate(blockTime: RFC3339, fees: Seq[Coin]): utilitiesFeeGrant.ValidateResponse = {
      val (delete, updatedAllowanceValue) = this.value.deleteAndUpdate(blockTime, fees)
      utilitiesFeeGrant.ValidateResponse(delete = delete, updated = this.copy(value = updatedAllowanceValue))
    }
  }

  implicit val allowanceReads: Reads[Allowance] = (
    (JsPath \ "allowanceType").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (allowanceApply _)

  implicit val allowanceWrites: Writes[Allowance] = Json.writes[Allowance]

  case class BasicAllowance(spendLimit: Seq[Coin], expiration: Option[RFC3339]) extends AbstarctFeeGrant.FeeAllowance {
    def getExpiration: Option[RFC3339] = expiration

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = {
      if (getExpiration.fold(false)(expiry => expiry.isBefore(blockTime)))
        (true, this)
      else if (spendLimit.nonEmpty) {
        val (left, _) = utilities.Blockchain.subtractCoins(spendLimit, fees)
        (left.exists(_.isZero), this.copy(spendLimit = left))
      } else (false, this)
    }
  }

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  implicit val basicAllowanceWrites: Writes[BasicAllowance] = Json.writes[BasicAllowance]

  case class PeriodicAllowance(basicAllowance: BasicAllowance, period: String, periodSpendLimit: Seq[Coin], periodCanSpend: Seq[Coin], periodReset: RFC3339) extends AbstarctFeeGrant.FeeAllowance {
    def getExpiration: Option[RFC3339] = basicAllowance.getExpiration

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = {
      if (getExpiration.fold(false)(expiry => blockTime.isAfter(expiry)))
        (true, this)
      else {
        val (resetPeriodCanSpend, updatedPeriodReset) = if (!blockTime.isBefore(this.periodReset)) {
          val (_, isNeg) = utilities.Blockchain.subtractCoins(fromCoins = this.basicAllowance.spendLimit, amount = this.periodSpendLimit)
          val resetPeriodCanSpend = if (isNeg && this.basicAllowance.spendLimit.nonEmpty) this.basicAllowance.spendLimit else this.periodSpendLimit
          val updatedPeriodReset = {
            val addPeriod = this.periodReset.addEpoch(utilities.Date.getEpoch(this.period))
            if (blockTime.isAfter(addPeriod)) blockTime.addEpoch(utilities.Date.getEpoch(this.period)) else addPeriod
          }
          (resetPeriodCanSpend, updatedPeriodReset)
        } else (this.periodCanSpend, this.periodReset)
        val (updatedPeriodCanSpend, _) = utilities.Blockchain.subtractCoins(fromCoins = resetPeriodCanSpend, amount = fees)
        if (this.basicAllowance.spendLimit.nonEmpty) {
          val (updatedBasicSpendLimit, _) = utilities.Blockchain.subtractCoins(fromCoins = this.basicAllowance.spendLimit, amount = fees)
          (updatedBasicSpendLimit.exists(_.isZero), this.copy(basicAllowance = this.basicAllowance.copy(spendLimit = updatedBasicSpendLimit), periodCanSpend = updatedPeriodCanSpend, periodReset = updatedPeriodReset))
        } else (false, this.copy(periodCanSpend = updatedPeriodCanSpend, periodReset = updatedPeriodReset))
      }
    }
  }

  implicit val periodicAllowanceReads: Reads[PeriodicAllowance] = Json.reads[PeriodicAllowance]

  implicit val periodicAllowanceWrites: Writes[PeriodicAllowance] = Json.writes[PeriodicAllowance]

  case class AllowedMsgAllowance(allowance: Allowance, allowedMessages: Seq[String]) extends AbstarctFeeGrant.FeeAllowance {
    def getExpiration: Option[RFC3339] = allowance.value.getExpiration

    def deleteAndUpdate(blockTime: RFC3339, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = this.allowance.value.deleteAndUpdate(blockTime, fees)
  }

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
