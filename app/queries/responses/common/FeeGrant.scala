package queries.responses.common

import models.common.{FeeGrant => commonFeeGrant}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.FeeGrant.FeeAllowance
import utilities.Date.RFC3339

object FeeGrant {

  implicit val module: String = constants.Module.RESPONSES_FEE_GRANT

  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  case class Allowance(allowanceType: String, value: FeeAllowance)

  implicit val allowanceReads: Reads[Allowance] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (allowanceApply _)

  case class BasicAllowance(spend_limit: Seq[Coin], expiration: Option[RFC3339]) extends FeeAllowance {
    def toSerializable: commonFeeGrant.BasicAllowance = commonFeeGrant.BasicAllowance(spendLimit = spend_limit.map(_.toCoin), expiration = expiration.fold(0L)(_.epoch))
  }

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  case class PeriodicAllowance(basic: BasicAllowance, period: String, period_spend_limit: Seq[Coin], period_can_spend: Seq[Coin], period_reset: RFC3339) extends FeeAllowance {
    def toSerializable: commonFeeGrant.PeriodicAllowance = commonFeeGrant.PeriodicAllowance(basicAllowance = basic.toSerializable, period = period.toLong, periodSpendLimit = period_spend_limit.map(_.toCoin), periodCanSpend = period_can_spend.map(_.toCoin), periodReset = period_reset.epoch)
  }

  implicit val periodicAllowanceReads: Reads[PeriodicAllowance] = Json.reads[PeriodicAllowance]

  case class AllowedMsgAllowance(allowance: Allowance, allowed_messages: Seq[String]) extends FeeAllowance {
    def toSerializable: commonFeeGrant.AllowedMsgAllowance = commonFeeGrant.AllowedMsgAllowance(allowance = allowance.value.toSerializable, allowedMessages = allowed_messages)
  }


  implicit val allowedMsgAllowanceReads: Reads[AllowedMsgAllowance] = Json.reads[AllowedMsgAllowance]

  def allowanceApply(allowanceType: String, value: JsObject): Allowance = allowanceType match {
    case constants.Blockchain.FeeGrant.BASIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[BasicAllowance](value.toString))
    case constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[PeriodicAllowance](value.toString))
    case constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[AllowedMsgAllowance](value.toString))
  }

}
