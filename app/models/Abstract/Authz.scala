package models.Abstract

import exceptions.BaseException
import models.common.Authz.{GenericAuthorization, SendAuthorization, StakeAuthorization}
import models.common.TransactionMessages.StdMsg
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}
import utilities.Blockchain.Authz.ValidateResponse

object Authz {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_AUTHZ

  implicit val logger: Logger = Logger(this.getClass)

  abstract class Authorization {

    def getAuthorizationType: String

    def getMsgTypeURL: String

    def validate(stdMsg: StdMsg): ValidateResponse

  }

  implicit val abstractAuthorizationWrites: Writes[Authorization] = {
    case sendAuthorization: SendAuthorization => Json.toJson(sendAuthorization)
    case genericAuthorization: GenericAuthorization => Json.toJson(genericAuthorization)
    case stakeAuthorization: StakeAuthorization => Json.toJson(stakeAuthorization)
    case _ => throw new BaseException(constants.Response.UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE)
  }

  implicit val abstractAuthorizationReads: Reads[Authorization] = {
    Json.format[SendAuthorization].map(x => x: Authorization) or
      Json.format[GenericAuthorization].map(x => x: Authorization) or
      Json.format[StakeAuthorization].map(x => x: Authorization)
  }
}
