package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.TransactionResponse.Msg
import queries.responses.common._
import models.blockchain.{WithdrawAddress => BlockchainWithdrawAddress}
import transactions.Abstract.BaseResponse

object GenesisResponse {

  case class GenTxValue(msg: Seq[Msg])

  implicit val genTxValueReads: Reads[GenTxValue] = Json.reads[GenTxValue]

  case class GenTx(value: GenTxValue)

  implicit val genTxReads: Reads[GenTx] = Json.reads[GenTx]

  case class Genutil(gentxs: Seq[GenTx])

  implicit val genUtilReads: Reads[Genutil] = Json.reads[Genutil]

  case class Bank(send_enabled: Boolean)

  implicit val bankReads: Reads[Bank] = Json.reads[Bank]

  case class Auth(accounts: Seq[Account.Result])

  implicit val authReads: Reads[Auth] = Json.reads[Auth]

  case class Staking(delegations: Seq[Delegation.Result], redelegations: Option[Seq[Redelegation.Result]], unbonding_delegations: Option[Seq[Undelegation.Result]], validators: Seq[Validator.Result])

  implicit val stakingReads: Reads[Staking] = Json.reads[Staking]

  case class WithdrawAddress(delegator_address: String, withdraw_address: String) {
    def toWithdrawAddress: BlockchainWithdrawAddress = BlockchainWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
  }

  implicit val withdrawAddressReads: Reads[WithdrawAddress] = Json.reads[WithdrawAddress]

  case class Distribution(delegator_withdraw_infos: Option[Seq[WithdrawAddress]])

  implicit val distributionReads: Reads[Distribution] = Json.reads[Distribution]

  case class AppState(bank: Bank, genutil: Genutil, auth: Auth, distribution: Distribution, staking: Staking)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
