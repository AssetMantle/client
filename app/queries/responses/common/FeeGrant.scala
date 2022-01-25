package queries.responses.common

import exceptions.BaseException
import models.common.{FeeGrant => commonFeeGrant}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.FeeGrant.FeeAllowance

object FeeGrant {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_RESPONSES_FEE_GRANT

  implicit val logger: Logger = Logger(this.getClass)

  case class Allowance(allowanceType: String, value: FeeAllowance) {
    def toSerializable: commonFeeGrant.Allowance = commonFeeGrant.Allowance(allowanceType = allowanceType, value = value.toSerializable)
  }

  implicit val allowanceReads: Reads[Allowance] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (allowanceApply _)

  case class BasicAllowance(spend_limit: Seq[Coin], expiration: Option[String]) extends FeeAllowance {
    def toSerializable: commonFeeGrant.BasicAllowance = commonFeeGrant.BasicAllowance(spendLimit = spend_limit.map(_.toCoin), expiration = expiration)
  }

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  case class PeriodicAllowance(basic: BasicAllowance, period: String, period_spend_limit: Seq[Coin], period_can_spend: Seq[Coin], period_reset: String) extends FeeAllowance {
    def toSerializable: commonFeeGrant.PeriodicAllowance = commonFeeGrant.PeriodicAllowance(basicAllowance = basic.toSerializable, period = period, periodSpendLimit = period_spend_limit.map(_.toCoin), periodCanSpend = period_can_spend.map(_.toCoin), periodReset = period_reset)
  }

  implicit val periodicAllowanceReads: Reads[PeriodicAllowance] = Json.reads[PeriodicAllowance]

  case class AllowedMsgAllowance(allowance: Allowance, allowed_messages: Seq[String]) extends FeeAllowance {
    def toSerializable: commonFeeGrant.AllowedMsgAllowance = commonFeeGrant.AllowedMsgAllowance(allowance = allowance.toSerializable, allowedMessages = allowed_messages)
  }

  implicit val allowedMsgAllowanceReads: Reads[AllowedMsgAllowance] = Json.reads[AllowedMsgAllowance]

  def allowanceApply(allowanceType: String, value: JsObject): Allowance = try {
    allowanceType match {
      case constants.Blockchain.FeeGrant.BASIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[BasicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[PeriodicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[AllowedMsgAllowance](value.toString))
      case _ => throw new BaseException(constants.Response.UNKNOWN_FEE_ALLOWANCE_RESPONSE_STRUCTURE)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.FEE_ALLOWANCE_RESPONSE_STRUCTURE_CHANGED)
  }

}
