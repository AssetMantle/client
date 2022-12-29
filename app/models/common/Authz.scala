package models.common

import exceptions.BaseException
import models.Abstract.{Authz => AuthzAbstract}
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.json._
import utilities.Blockchain.Authz.ValidateResponse
import utilities.MicroNumber

object Authz {
  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_AUTHZ

  implicit val logger: Logger = Logger(this.getClass)

  //bank
  case class SendAuthorization(spendLimit: Seq[Coin]) extends AuthzAbstract.Authorization {

    def getAuthorizationType: String = constants.Blockchain.Authz.SEND_AUTHORIZATION

    def getMsgTypeURL: String = constants.Blockchain.TransactionMessage.SEND_COIN

    def validate(stdMsg: TransactionMessages.StdMsg): ValidateResponse = {
      val (limitLeft, _) = utilities.Blockchain.subtractCoins(spendLimit, stdMsg.message.asInstanceOf[SendCoin].amount)
      if (limitLeft.exists(_.isZero)) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(SendAuthorization(spendLimit = limitLeft)))
    }
  }

  implicit val sendAuthorizationReads: Reads[SendAuthorization] = Json.reads[SendAuthorization]

  implicit val sendAuthorizationWrites: Writes[SendAuthorization] = Json.writes[SendAuthorization]

  //authz
  case class GenericAuthorization(message: String) extends AuthzAbstract.Authorization {
    def getAuthorizationType: String = constants.Blockchain.Authz.GENERIC_AUTHORIZATION

    def getMsgTypeURL: String = message

    def validate(stdMsg: TransactionMessages.StdMsg): ValidateResponse = ValidateResponse(accept = true, delete = false, updated = None)
  }

  implicit val genericAuthorizationReads: Reads[GenericAuthorization] = Json.reads[GenericAuthorization]

  implicit val genericAuthorizationWrites: Writes[GenericAuthorization] = Json.writes[GenericAuthorization]

  //staking
  case class StakeAuthorizationValidators(address: Seq[String])

  implicit val stakeAuthorizationValidatorsReads: Reads[StakeAuthorizationValidators] = Json.reads[StakeAuthorizationValidators]

  implicit val stakeAuthorizationValidatorsWrites: Writes[StakeAuthorizationValidators] = Json.writes[StakeAuthorizationValidators]

  case class StakeAuthorization(maxTokens: Option[Coin], allowList: Option[StakeAuthorizationValidators], denyList: Option[StakeAuthorizationValidators], authorizationType: String) extends AuthzAbstract.Authorization {

    def getAuthorizationType: String = constants.Blockchain.Authz.STAKE_AUTHORIZATION

    def getMsgTypeURL: String = authorizationType match {
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_DELEGATE => constants.Blockchain.TransactionMessage.DELEGATE
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_UNDELEGATE => constants.Blockchain.TransactionMessage.UNDELEGATE
      case constants.Blockchain.Authz.StakeAuthorization.AUTHORIZATION_TYPE_REDELEGATE => constants.Blockchain.TransactionMessage.REDELEGATE
      case _ => throw new BaseException(constants.Response.TRANSACTION_PROCESSING_FAILED)
    }

    def validate(stdMsg: TransactionMessages.StdMsg): ValidateResponse = if (maxTokens.isEmpty) {
      ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = None)))
    } else {
      val amount = getMsgTypeURL match {
        case constants.Blockchain.TransactionMessage.DELEGATE => stdMsg.message.asInstanceOf[Delegate].amount
        case constants.Blockchain.TransactionMessage.UNDELEGATE => stdMsg.message.asInstanceOf[Undelegate].amount
        case constants.Blockchain.TransactionMessage.REDELEGATE => stdMsg.message.asInstanceOf[Redelegate].amount
        case _ => throw new BaseException(constants.Response.TRANSACTION_PROCESSING_FAILED)
      }
      val limitLeft = maxTokens.getOrElse(Coin("", MicroNumber.zero).subtract(amount))
      if (limitLeft.isZero) ValidateResponse(accept = true, delete = true, updated = None)
      else ValidateResponse(accept = true, delete = false, updated = Option(this.copy(maxTokens = Option(limitLeft))))
    }
  }

  implicit val stakeAuthorizationReads: Reads[StakeAuthorization] = Json.reads[StakeAuthorization]

  implicit val stakeAuthorizationWrites: Writes[StakeAuthorization] = Json.writes[StakeAuthorization]

  case class Authorization(authorizationType: String, value: AuthzAbstract.Authorization)

  implicit val authorizationReads: Reads[Authorization] = Json.reads[Authorization]

  implicit val authorizationWrites: Writes[Authorization] = Json.writes[Authorization]
}
