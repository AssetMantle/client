package queries.responses.common

import exceptions.BaseException
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.Abstract.FeeGrant.FeeAllowance
import queries.responses.common.TransactionMessageResponses.Allowance

object FeeGrant {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_RESPONSES_FEE_GRANT

  implicit val logger: Logger = Logger(this.getClass)

  case class BasicAllowance(spend_limit: Seq[Coin], expiration: Option[String]) extends FeeAllowance

  implicit val basicAllowanceReads: Reads[BasicAllowance] = Json.reads[BasicAllowance]

  case class PeriodicAllowance(basic: BasicAllowance, period: String, period_spend_limit: Seq[Coin], period_can_spend: Seq[Coin], period_reset: String) extends FeeAllowance

  implicit val periodicAllowanceReads: Reads[PeriodicAllowance] = Json.reads[PeriodicAllowance]

  case class AllowedMsgAllowance(allowance: FeeAllowance, allowed_messages: Seq[String]) extends FeeAllowance

  implicit val allowedMsgAllowanceReads: Reads[AllowedMsgAllowance] = Json.reads[AllowedMsgAllowance]

  def allowanceApply(allowanceType: String, value: JsObject): Allowance = try {
    allowanceType match {
      case constants.Blockchain.FeeGrant.BASIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[BasicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.PERIODIC_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[PeriodicAllowance](value.toString))
      case constants.Blockchain.FeeGrant.ALLOWED_MSG_ALLOWANCE => Allowance(allowanceType, utilities.JSON.convertJsonStringToObject[AllowedMsgAllowance](value.toString))
      case _ => throw new BaseException(constants.Response.UNKNOWN_FEE_ALLOWANCE_RESPONSE_STRUCTURE_CHANGED)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.FEE_ALLOWANCE_RESPONSE_STRUCTURE_CHANGED)
  }
}
