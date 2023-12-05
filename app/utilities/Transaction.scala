package utilities

import com.google.protobuf.{Any => protoBufAny}
import models.blockchain.Account
import models.common.Serializable.Coin
import org.bitcoinj.core.ECKey
import queries.blockchain.{GetABCIInfo, GetAccount, GetUnconfirmedTxs}
import queries.responses.blockchain.UnconfirmedTxsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Transaction @Inject()(
                             getUnconfirmedTxs: GetUnconfirmedTxs,
                             getAccount: GetAccount,
                             getAbciInfo: GetABCIInfo,
                             utilitiesOperations: utilities.Operations,
                           )(private implicit val executionContext: ExecutionContext) {

  def getLatestHeightAccountAndUnconfirmedTxs(address: String): Future[(Int, Account, UnconfirmedTxsResponse.Response)] = {
    // TODO
    // val bcAccount = blockchainAccounts.Service.tryGet(fromAddress)
    val abciInfo = getAbciInfo.Service.get
    val bcAccount = getAccount.Service.get(address).map(_.account.toSerializableAccount).recover {
      case _: Exception => models.blockchain.Account(address = address, accountType = None, accountNumber = 0, sequence = 0, publicKey = None, vestingParameters = None, publicKeyType = None)
    }
    val unconfirmedTxs = getUnconfirmedTxs.Service.get()

    for {
      abciInfo <- abciInfo
      bcAccount <- bcAccount
      unconfirmedTxs <- unconfirmedTxs
    } yield (abciInfo.result.response.last_block_height.toInt, bcAccount, unconfirmedTxs)
  }

  def getFee(gasPrice: BigDecimal, denom: String, gasLimit: Int): Coin = Coin(denom = denom, amount = MicroNumber((gasPrice * gasLimit) / MicroNumber.factor))

  def getTx(latestBlockHeight: Int, messages: Seq[protoBufAny], gasPrice: BigDecimal, gasLimit: Int, bcAccount: models.blockchain.Account, ecKey: ECKey, memo: String): (Int, Array[Byte], String) = {
    val timeoutHeight = latestBlockHeight + 100
    val finalGasLimit = gasLimit * messages.length
    val txRawBytes = utilities.BlockchainTransaction.getTxRawBytes(
      messages = messages,
      fee = getFee(gasPrice = gasPrice, denom = "umnlt", gasLimit = finalGasLimit),
      gasLimit = finalGasLimit,
      account = bcAccount,
      ecKey = ecKey,
      timeoutHeight = timeoutHeight,
      memo = memo)
    val txHash = utilities.Secrets.sha256HashHexString(txRawBytes)
    (timeoutHeight, txRawBytes, txHash)
  }

}
