package queries.responses.common

import play.api.libs.json.{Json, Reads}

object Authorization {

  case class Send(spend_limit: Seq[Coin])

  case class Generic(msg: String)


  case class StakeAuthorizationValidators(address: Seq[String])

  implicit val stakeAuthorizationValidatorsReads: Reads[StakeAuthorizationValidators] = Json.reads[StakeAuthorizationValidators]

  case class StakeAuthorizationAllowList(allow_list: StakeAuthorizationValidators)

  implicit val stakeAuthorizationAllowListReads: Reads[StakeAuthorizationAllowList] = Json.reads[StakeAuthorizationAllowList]

  case class StakeAuthorizationDenyList(deny_list: StakeAuthorizationValidators)

  implicit val stakeAuthorizationDenyListReads: Reads[StakeAuthorizationDenyList] = Json.reads[StakeAuthorizationDenyList]

  case class Stake(max_tokens: Seq[Coin], validators:, authorization_type:)
}
