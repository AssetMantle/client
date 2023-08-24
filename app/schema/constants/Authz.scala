package schema.constants

object Authz {
  val SEND_AUTHORIZATION = "/cosmos.bank.v1beta1.SendAuthorization"
  val GENERIC_AUTHORIZATION = "/cosmos.authz.v1beta1.GenericAuthorization"
  val STAKE_AUTHORIZATION = "/cosmos.staking.v1beta1.StakeAuthorization"

  object StakeAuthorization {
    val AUTHORIZATION_TYPE_DELEGATE = "AUTHORIZATION_TYPE_DELEGATE"
    val AUTHORIZATION_TYPE_UNDELEGATE = "AUTHORIZATION_TYPE_UNDELEGATE"
    val AUTHORIZATION_TYPE_REDELEGATE = "AUTHORIZATION_TYPE_REDELEGATE"
  }
}