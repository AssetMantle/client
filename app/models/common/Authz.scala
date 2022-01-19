package models.common

import exceptions.BaseException
import models.Abstract.{Authz => AuthzAbstract}
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json._

object Authz {
  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_AUTHZ

  implicit val logger: Logger = Logger(this.getClass)

  //bank
  case class SendAuthorization(spendLimit: Seq[Coin]) extends AuthzAbstract.Authorization

  implicit val sendAuthorizationReads: Reads[SendAuthorization] = Json.reads[SendAuthorization]

  implicit val sendAuthorizationWrites: Writes[SendAuthorization] = Json.writes[SendAuthorization]

  //authz
  case class GenericAuthorization(message: String) extends AuthzAbstract.Authorization

  implicit val genericAuthorizationReads: Reads[GenericAuthorization] = Json.reads[GenericAuthorization]

  implicit val genericAuthorizationWrites: Writes[GenericAuthorization] = Json.writes[GenericAuthorization]

  //staking
  case class StakeAuthorizationValidators(address: Seq[String])

  implicit val stakeAuthorizationValidatorsReads: Reads[StakeAuthorizationValidators] = Json.reads[StakeAuthorizationValidators]

  implicit val stakeAuthorizationValidatorsWrites: Writes[StakeAuthorizationValidators] = Json.writes[StakeAuthorizationValidators]

  case class StakeAuthorization(maxTokens: Option[Coin], allowList: Option[StakeAuthorizationValidators], denyList: Option[StakeAuthorizationValidators], authorizationType: String) extends AuthzAbstract.Authorization

  implicit val stakeAuthorizationReads: Reads[StakeAuthorization] = Json.reads[StakeAuthorization]

  implicit val stakeAuthorizationWrites: Writes[StakeAuthorization] = Json.writes[StakeAuthorization]

  implicit val abstractAuthorizationWrites: Writes[AuthzAbstract.Authorization] = {
    case sendAuthorization: SendAuthorization => Json.toJson(sendAuthorization)
    case genericAuthorization: GenericAuthorization => Json.toJson(genericAuthorization)
    case stakeAuthorization: StakeAuthorization => Json.toJson(stakeAuthorization)
    case _ => throw new BaseException(constants.Response.UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE)
  }

  implicit val abstractAuthorizationReads: Reads[AuthzAbstract.Authorization] = {
    Json.format[SendAuthorization].map(x => x: AuthzAbstract.Authorization) or
      Json.format[GenericAuthorization].map(x => x: AuthzAbstract.Authorization) or
      Json.format[StakeAuthorization].map(x => x: AuthzAbstract.Authorization)
  }

  case class Authorization(authorizationType: String, value: AuthzAbstract.Authorization)

  implicit val authorizationReads: Reads[Authorization] = Json.reads[Authorization]

  implicit val authorizationWrites: Writes[Authorization] = Json.writes[Authorization]
}
