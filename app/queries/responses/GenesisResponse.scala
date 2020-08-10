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

  case class Balance(address: String, coins: Seq[Coin])

  implicit val balanceReads: Reads[Balance] = Json.reads[Balance]

  case class Bank(balances: Seq[Balance])

  implicit val bankReads: Reads[Bank] = Json.reads[Bank]

  case class AppState(bank: Bank, genutil: Genutil)

  implicit val appStateReads: Reads[AppState] = Json.reads[AppState]

  case class Genesis(app_state: AppState)

  implicit val genesisReads: Reads[Genesis] = Json.reads[Genesis]

  case class Result(genesis: Genesis)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
