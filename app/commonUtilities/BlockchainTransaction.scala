package commonUtilities

import com.cosmos.bank.v1beta1.MsgSend
import com.cosmos.base.v1beta1.Coin
import com.cosmos.crypto.secp256k1
import com.cosmos.tx.v1beta1._
import com.google.protobuf.{ByteString, Any => protoBufAny}
import org.bitcoinj.core.ECKey

import scala.jdk.CollectionConverters.IterableHasAsJava

object BlockchainTransaction {

  private def getTxRawBytes(messages: Seq[protoBufAny], fee: Coin, gasLimit: Int, accountNumber: Int, sequence: Int, ecKey: ECKey, memo: String, chainID: String): Array[Byte] = {
    val txBody = TxBody.newBuilder().addAllMessages(messages.asJava).setMemo(memo).build()

    val signerInfo = SignerInfo.newBuilder()
      .setSequence(sequence)
      .setPublicKey(com.google.protobuf.Any.newBuilder().setTypeUrl(commonConstants.Blockchain.PublicKey.SINGLE_SECP256K1).setValue(secp256k1.PubKey.newBuilder().setKey(ByteString.copyFrom(ecKey.getPubKey)).build().toByteString).build())
      .setModeInfo(ModeInfo.newBuilder().setSingle(ModeInfo.Single.newBuilder().setModeValue(1).build()).build())
      .build()

    val authInfo = AuthInfo.newBuilder()
      .addSignerInfos(signerInfo)
      .setFee(Fee.newBuilder().addAmount(fee).setGasLimit(gasLimit).build())
      .build()

    val signDoc = SignDoc.newBuilder()
      .setBodyBytes(txBody.toByteString)
      .setAuthInfoBytes(authInfo.toByteString)
      .setChainId(chainID)
      .setAccountNumber(accountNumber)
      .build()

    val txRaw = TxRaw.newBuilder()
      .setBodyBytes(txBody.toByteString)
      .setAuthInfoBytes(authInfo.toByteString)
      .addSignatures(ByteString.copyFrom(Wallet.ecdsaSign(commonUtilities.Secrets.sha256Hash(signDoc.toByteArray), ecKey)))
      .build()
    txRaw.toByteArray
  }

  def getTxRawBytesWithSignedMemo(messages: Seq[protoBufAny], fee: Coin, gasLimit: Int, accountNumber: Int, sequence: Int, ecKey: ECKey, chainID: String, memoSignerPrivateKey: Array[Byte]): (Array[Byte], String) = {
    val txRawBytesWithoutMemo = getTxRawBytes(
      messages = messages,
      fee = fee,
      gasLimit = gasLimit,
      accountNumber = accountNumber,
      sequence = sequence,
      ecKey = ecKey,
      memo = "",
      chainID = chainID)
    val memo = commonUtilities.Secrets.base64URLEncoder(commonUtilities.Wallet.ecdsaSign(commonUtilities.Secrets.sha256Hash(txRawBytesWithoutMemo), ECKey.fromPrivate(memoSignerPrivateKey)))
    (getTxRawBytes(
      messages = messages,
      fee = fee,
      gasLimit = gasLimit,
      accountNumber = accountNumber,
      sequence = sequence,
      ecKey = ecKey,
      memo = memo,
      chainID = chainID), memo)
  }

  def memoGenerator(memoPrefix: String, memoSignerPrivateKey: Array[Byte]): String = commonUtilities.Secrets.base64URLEncoder(commonUtilities.Wallet.hashAndEcdsaSign(memoPrefix, ECKey.fromPrivate(memoSignerPrivateKey)))

  def getFee(gasPrice: Double, stakingDenom: String, gasLimit: Int): Coin = Coin.newBuilder().setDenom(stakingDenom).setDenom(MicroNumber(gasPrice * gasLimit).toMicroString).build()

  def getSendCoinMsgAsAny(fromAddress: String, toAddress: String, amount: Seq[Coin]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(commonConstants.Blockchain.TransactionMessage.SEND_COIN)
    .setValue(MsgSend.newBuilder()
      .setFromAddress(fromAddress)
      .setToAddress(toAddress)
      .addAllAmount(amount.asJava)
      .build().toByteString)
    .build()

  def getMintMsgAsAny(fromAddress: String, fromId: String, toId: String, classificationId: String): protoBufAny = protoBufAny.newBuilder()
    //    .setTypeUrl(constants.Blockchain.TransactionMessage.MINT)
    //    .setValue(Tx
    //      .MsgSend.newBuilder()
    //      .setFromAddress(fromAddress)
    //      .setToAddress(toAddress)
    //      .addAllAmount(amount.map(_.toProtoCoin).asJava)
    //      .build().toByteString)
    .build()

  def getNubMsgAsAny(fromAddress: String, fromId: String, toId: String, classificationId: String): protoBufAny = protoBufAny.newBuilder()
    //    .setTypeUrl(constants.Blockchain.TransactionMessage.MINT)
    //    .setValue(Tx
    //      .MsgSend.newBuilder()
    //      .setFromAddress(fromAddress)
    //      .setToAddress(toAddress)
    //      .addAllAmount(amount.map(_.toProtoCoin).asJava)
    //      .build().toByteString)
    .build()

}
