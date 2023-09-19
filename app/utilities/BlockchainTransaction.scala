package utilities

import com.assetmantle.modules.identities.transactions.{provision, unprovision}
import com.cosmos.authz.v1beta1.MsgExec
import com.cosmos.bank.v1beta1.MsgSend
import com.cosmos.crypto.secp256k1.PubKey
import com.cosmos.tx.v1beta1._
import com.google.protobuf.{ByteString, Any => protoBufAny}
import models.common.Serializable.Coin
import org.bitcoinj.core.ECKey
import schema.id.base.IdentityID

import scala.jdk.CollectionConverters.IterableHasAsJava

object BlockchainTransaction {

  def getTxRawBytes(messages: Seq[protoBufAny], fee: Coin, gasLimit: Int, account: models.blockchain.Account, ecKey: ECKey, memo: String, timeoutHeight: Int): Array[Byte] = {
    val txBody = TxBody.newBuilder().addAllMessages(messages.asJava).setMemo(memo).setTimeoutHeight(timeoutHeight.toLong).build()

    val signerInfo = SignerInfo.newBuilder()
      .setSequence(account.sequence)
      .setPublicKey(com.google.protobuf.Any.newBuilder().setTypeUrl(schema.constants.PublicKey.SINGLE_SECP256K1).setValue(PubKey.newBuilder().setKey(ByteString.copyFrom(ecKey.getPubKey)).build().toByteString).build())
      .setModeInfo(ModeInfo.newBuilder().setSingle(ModeInfo.Single.newBuilder().setModeValue(1).build()).build())
      .build()

    val authInfo = AuthInfo.newBuilder()
      .addSignerInfos(signerInfo)
      .setFee(Fee.newBuilder().addAmount(fee.toProtoCoin).setGasLimit(gasLimit).build())
      .build()

    val signDoc = SignDoc.newBuilder()
      .setBodyBytes(txBody.toByteString)
      .setAuthInfoBytes(authInfo.toByteString)
      .setChainId(constants.Blockchain.ChainID)
      .setAccountNumber(account.accountNumber)
      .build()

    val txRaw = TxRaw.newBuilder()
      .setBodyBytes(txBody.toByteString)
      .setAuthInfoBytes(authInfo.toByteString)
      .addSignatures(ByteString.copyFrom(Wallet.ecdsaSign(Secrets.sha256Hash(signDoc.toByteArray), ecKey)))
      .build()
    txRaw.toByteArray
  }

  def getSendCoinMsgAsAny(fromAddress: String, toAddress: String, amount: Seq[Coin]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.SEND_COIN)
    .setValue(MsgSend
      .newBuilder()
      .setFromAddress(fromAddress)
      .setToAddress(toAddress)
      .addAllAmount(amount.map(_.toProtoCoin).asJava)
      .build().toByteString)
    .build()

  def getProvisionMsg(fromAddress: String, fromID: IdentityID, toAddress: String): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.IDENTITY_PROVISION)
    .setValue(provision
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setTo(toAddress)
      .setIdentityID(fromID.asProtoIdentityID)
      .build().toByteString)
    .build()

  def getUnprovisionMsg(fromAddress: String, fromID: IdentityID, toAddress: String): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.IDENTITY_UNPROVISION)
    .setValue(unprovision
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setTo(toAddress)
      .setIdentityID(fromID.asProtoIdentityID)
      .build().toByteString)
    .build()


  def getExecuteMsg(fromAddress: String, messages: Seq[protoBufAny]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.EXECUTE_AUTHORIZATION)
    .setValue(MsgExec.newBuilder()
      .setGrantee(fromAddress)
      .addAllMsgs(messages.asJava)
      .build().toByteString)
    .build()
}
