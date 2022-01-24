package models.common

import models.Abstract.{FeeGrant => AbstarctFeeGrant}
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import utilities.Blockchain.{FeeGrant => utilitiesFeeGrant}

object FeeGrant {

  private implicit val module: String = constants.Module.TRANSACTION_MESSAGE_FEE_GRANT

  private implicit val logger: Logger = Logger(this.getClass)

  case class Allowance(allowanceType: String, value: AbstarctFeeGrant.FeeAllowance) {
    def validate(blockTime: String, fees: Seq[Coin]): utilitiesFeeGrant.ValidateResponse = {
      val (delete, updatedAllowanceValue) = this.value.validate(blockTime, fees)
      utilitiesFeeGrant.ValidateResponse(delete = delete, updated = this.copy(value = updatedAllowanceValue))
    }
  }

  implicit val allowanceReads: Reads[Allowance] = (
    (JsPath \ "allowanceType").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (allowanceApply _)

  implicit val allowanceWrites: Writes[Allowance] = Json.writes[Allowance]

  case class BasicAllowance(spendLimit: Seq[Coin], expiration: Option[String]) extends AbstarctFeeGrant.FeeAllowance {
    def getExpiration: Option[String] = expiration

    def validate(blockTime: String, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = {
      if (getExpiration.nonEmpty && utilities.Date.isBefore(t1 = getExpiration.getOrElse(""), t2 = blockTime))
        (true, this)
      else if (spendLimit.nonEmpty) {
        val (left, _) = utilities.Blockchain.subtractCoins(spendLimit, fees)
        (left.exists(_.isZero), this.copy(spendLimit = left))
      } else (false, this)
    }
  }

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  implicit val basicAllowanceWrites: Writes[BasicAllowance] = Json.writes[BasicAllowance]

  case class PeriodicAllowance(basicAllowance: BasicAllowance, period: String, periodSpendLimit: Seq[Coin], periodCanSpend: Seq[Coin], periodReset: String) extends AbstarctFeeGrant.FeeAllowance {
    def getExpiration: Option[String] = basicAllowance.getExpiration

    def validate(blockTime: String, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = {
      if (getExpiration.nonEmpty && utilities.Date.isAfter(t1 = blockTime, t2 = getExpiration.getOrElse("")))
        (true, this)
      else {
        val (resetPeriodCanSpend, updatedPeriodReset) = if (!utilities.Date.isBefore(t1 = blockTime, t2 = this.periodReset)) {
          val (_, isNeg) = utilities.Blockchain.subtractCoins(fromCoins = this.basicAllowance.spendLimit, amount = this.periodSpendLimit)
          val resetPeriodCanSpend = if (isNeg && this.basicAllowance.spendLimit.nonEmpty) this.basicAllowance.spendLimit else this.periodSpendLimit
          val updatedPeriodReset = {
            val addPeriod = utilities.Date.addTime(timestamp = this.periodReset, addEpochTime = utilities.Date.getEpoch(this.period))
            if (utilities.Date.isAfter(t1 = blockTime, t2 = addPeriod)) utilities.Date.addTime(blockTime, utilities.Date.getEpoch(this.period)) else addPeriod
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
    def getExpiration: Option[String] = allowance.value.getExpiration

    def validate(blockTime: String, fees: Seq[Coin]): (Boolean, AbstarctFeeGrant.FeeAllowance) = this.allowance.value.validate(blockTime, fees)
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
