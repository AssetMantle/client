package queries.responses.common

import exceptions.BaseException
import models.common.{Serializable, Authz => SerializableAuthz}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.{Authz => AuthzAbstract}

object Authz {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_RESPONSES_AUTHZ

  implicit val logger: Logger = Logger(this.getClass)

  //bank
  case class SendAuthorization(spend_limit: Seq[Coin]) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.SendAuthorization = SerializableAuthz.SendAuthorization(spendLimit = spend_limit.map(_.toCoin))
  }

  implicit val sendAuthorizationReads: Reads[SendAuthorization] = Json.reads[SendAuthorization]

  //authz
  case class GenericAuthorization(msg: String) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.GenericAuthorization = SerializableAuthz.GenericAuthorization(message = msg)
  }

  implicit val genericAuthorizationReads: Reads[GenericAuthorization] = Json.reads[GenericAuthorization]

  //staking
  case class StakeAuthorizationValidators(address: Seq[String]) {
    def toSerializable: SerializableAuthz.StakeAuthorizationValidators = SerializableAuthz.StakeAuthorizationValidators(address = address)
  }

  implicit val stakeAuthorizationValidatorsReads: Reads[StakeAuthorizationValidators] = Json.reads[StakeAuthorizationValidators]

  case class StakeAuthorization(max_tokens: Option[Coin], allow_list: Option[StakeAuthorizationValidators], deny_list: Option[StakeAuthorizationValidators], authorization_type: String) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.StakeAuthorization = SerializableAuthz.StakeAuthorization(maxTokens = max_tokens.fold[Option[Serializable.Coin]](None)(x => Option(x.toCoin)), allowList = allow_list.fold[Option[SerializableAuthz.StakeAuthorizationValidators]](None)(x => Option(x.toSerializable)), denyList = deny_list.fold[Option[SerializableAuthz.StakeAuthorizationValidators]](None)(x => Option(x.toSerializable)), authorizationType = authorization_type)
  }

  implicit val stakeAuthorizationReads: Reads[StakeAuthorization] = Json.reads[StakeAuthorization]

  case class Authorization(authorizationType: String, value: AuthzAbstract.Authorization) {
    def toSerializable: SerializableAuthz.Authorization = SerializableAuthz.Authorization(authorizationType = authorizationType, value = value.toSerializable)
  }

  def authorizationApply(authorizationType: String, value: JsObject): Authorization = try {
    authorizationType match {
      case constants.Blockchain.Authz.SEND_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[SendAuthorization](value.toString))
      case constants.Blockchain.Authz.GENERIC_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[GenericAuthorization](value.toString))
      case constants.Blockchain.Authz.STAKE_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[StakeAuthorization](value.toString))
      case _ => throw new BaseException(constants.Response.UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.GRANT_AUTHORIZATION_RESPONSE_STRUCTURE_CHANGED)
  }

  implicit val authorizationReads: Reads[Authorization] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (authorizationApply _)
}
