package queries.responses.common

import models.Abstract.{Authorization => commonAuthzAbstract}
import models.common.{Serializable, Authz => SerializableAuthz}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.{Authz => AuthzAbstract}
import utilities.MicroNumber

object Authz {

  implicit val module: String = constants.Module.RESPONSES_AUTHZ

  implicit val logger: Logger = Logger(this.getClass)

  //bank
  case class SendAuthorization(spend_limit: Seq[Coin]) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.SendAuthorization = SerializableAuthz.SendAuthorization(spendLimit = spend_limit.map(_.toCoin))
  }

  implicit val sendAuthorizationReads: Reads[SendAuthorization] = Json.reads[SendAuthorization]

  //authz
  case class GenericAuthorization(msg: String) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.GenericAuthorization = SerializableAuthz.GenericAuthorization(msg = msg)
  }

  implicit val genericAuthorizationReads: Reads[GenericAuthorization] = Json.reads[GenericAuthorization]

  //staking
  case class StakeAuthorizationValidators(address: Seq[String]) {
    def toSerializable: SerializableAuthz.StakeAuthorizationValidators = SerializableAuthz.StakeAuthorizationValidators(address = address)
  }

  implicit val stakeAuthorizationValidatorsReads: Reads[StakeAuthorizationValidators] = Json.reads[StakeAuthorizationValidators]

  case class StakeAuthorization(max_tokens: Option[Coin], allow_list: Option[StakeAuthorizationValidators], deny_list: Option[StakeAuthorizationValidators], authorization_type: String) extends AuthzAbstract.Authorization {
    def toSerializable: SerializableAuthz.StakeAuthorization = SerializableAuthz.StakeAuthorization(maxTokens = this.max_tokens.fold(Serializable.Coin("", MicroNumber.zero))(_.toCoin), allowList = this.allow_list.fold(SerializableAuthz.StakeAuthorizationValidators(Seq.empty))(_.toSerializable), denyList = this.deny_list.fold(SerializableAuthz.StakeAuthorizationValidators(Seq()))(_.toSerializable), authorizationType = authorization_type)
  }

  implicit val stakeAuthorizationReads: Reads[StakeAuthorization] = Json.reads[StakeAuthorization]

  case class Authorization(authorizationType: String, value: AuthzAbstract.Authorization) {
    def toSerializable: commonAuthzAbstract = this.value.toSerializable
  }

  def authorizationApply(authorizationType: String, value: JsObject): Authorization = try {
    authorizationType match {
      case schema.constants.Authz.SEND_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[SendAuthorization](value.toString))
      case schema.constants.Authz.GENERIC_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[GenericAuthorization](value.toString))
      case schema.constants.Authz.STAKE_AUTHORIZATION => Authorization(authorizationType, utilities.JSON.convertJsonStringToObject[StakeAuthorization](value.toString))
      case _ => constants.Response.UNKNOWN_GRANT_AUTHORIZATION_RESPONSE_STRUCTURE.throwBaseException()
    }
  } catch {
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      constants.Response.GRANT_AUTHORIZATION_RESPONSE_STRUCTURE_CHANGED.throwBaseException()
  }

  implicit val authorizationReads: Reads[Authorization] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (authorizationApply _)
}
