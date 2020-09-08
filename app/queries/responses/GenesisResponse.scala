package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.TransactionResponse.Msg
import queries.responses.common.Coin
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

  case class AccountValue(address: String, coins: Seq[Coin])

  implicit val accountValueReads: Reads[AccountValue] = Json.reads[AccountValue]

  //TODO: make it according to account response
  case class Account(value: AccountValue)

  implicit val accountReads: Reads[Account] = Json.reads[Account]

  case class Auth(accounts: Seq[Account])

  implicit val authReads: Reads[Auth] = Json.reads[Auth]

  case class AppState(bank: Bank, genutil: Genutil, auth: Auth)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
