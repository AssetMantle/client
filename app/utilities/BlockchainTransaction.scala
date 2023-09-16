package utilities

import com.assetmantle.modules.assets.transactions.{unwrap, wrap, define => assetDefine, mint => mintAsset, send => assetSend}
import com.assetmantle.modules.identities.transactions.{issue, provision, unprovision}
import com.assetmantle.modules.orders.transactions.{get, put, cancel => orderCancel}
import com.cosmos.authz.v1beta1.MsgExec
import com.cosmos.bank.v1beta1.MsgSend
import com.cosmos.crypto.secp256k1.PubKey
import com.cosmos.tx.v1beta1._
import com.google.protobuf.{ByteString, Any => protoBufAny}
import models.common.Serializable.Coin
import org.bitcoinj.core.ECKey
import play.api.Logger
import schema.data.base.NumberData
import schema.id.base.{AssetID, ClassificationID, IdentityID, OrderID}
import schema.list.PropertyList
import schema.property.base.{MesaProperty, MetaProperty}
import schema.qualified.{Immutables, Mutables}
import schema.types.Height

import java.security.MessageDigest
import scala.jdk.CollectionConverters.IterableHasAsJava

object BlockchainTransaction {

  def getTxRawBytes(messages: Seq[protoBufAny], fee: Coin, gasLimit: Int, account: models.blockchain.Account, ecKey: ECKey, memo: String, timeoutHeight: Int): Array[Byte] = {
    val txBody = TxBody.newBuilder().addAllMessages(messages.asJava).setMemo(memo).setTimeoutHeight(timeoutHeight.toLong).build()

    val signerInfo = SignerInfo.newBuilder()
      .setSequence(account.sequence)
      .setPublicKey(com.google.protobuf.Any.newBuilder().setTypeUrl(constants.Blockchain.PublicKey.SINGLE_SECP256K1).setValue(PubKey.newBuilder().setKey(ByteString.copyFrom(ecKey.getPubKey)).build().toByteString).build())
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

  def getIssueIdentityMsgWithAuthentication(fromAddress: String, fromID: IdentityID, classificationID: ClassificationID, immutableMetas: Immutables, mutableMetas: Mutables, immutableMesas: Immutables, mutableMesas: Mutables)(implicit module: String, logger: Logger): protoBufAny = {
    protoBufAny.newBuilder()
      .setTypeUrl(schema.constants.Messages.IDENTITY_ISSUE)
      .setValue(issue
        .Message.newBuilder()
        .setFrom(fromAddress)
        .setFromID(fromID.asProtoIdentityID)
        .setClassificationID(classificationID.asProtoClassificationID)
        .setImmutableMetaProperties(immutableMetas.propertyList.asProtoPropertyList)
        .setImmutableProperties(immutableMesas.propertyList.asProtoPropertyList)
        .setMutableMetaProperties(mutableMetas.propertyList.asProtoPropertyList)
        .setMutableProperties(mutableMesas.propertyList.asProtoPropertyList)
        .build().toByteString)
      .build()
  }

  def getDefineAssetMsg(fromAddress: String, fromID: IdentityID, immutableMetas: Seq[MetaProperty], immutables: Seq[MesaProperty], mutableMetas: Seq[MetaProperty], mutables: Seq[MesaProperty]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ASSET_DEFINE)
    .setValue(assetDefine
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setImmutableMetaProperties(PropertyList(immutableMetas).asProtoPropertyList)
      .setImmutableProperties(PropertyList(immutables).asProtoPropertyList)
      .setMutableMetaProperties(PropertyList(mutableMetas).asProtoPropertyList)
      .setMutableProperties(PropertyList(mutables).asProtoPropertyList)
      .build().toByteString)
    .build()

  def getMintAssetMsg(fromAddress: String, fromID: IdentityID, classificationID: ClassificationID, toID: IdentityID, immutableMetas: Seq[MetaProperty], immutables: Seq[MesaProperty], mutableMetas: Seq[MetaProperty], mutables: Seq[MesaProperty]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ASSET_MINT)
    .setValue(mintAsset
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setToID(toID.asProtoIdentityID)
      .setClassificationID(classificationID.asProtoClassificationID)
      .setImmutableMetaProperties(PropertyList(immutableMetas).asProtoPropertyList)
      .setImmutableProperties(PropertyList(immutables).asProtoPropertyList)
      .setMutableMetaProperties(PropertyList(mutableMetas).asProtoPropertyList)
      .setMutableProperties(PropertyList(mutables).asProtoPropertyList)
      .build().toByteString)
    .build()

  def getWrapTokenMsg(fromAddress: String, fromID: IdentityID, coins: Seq[Coin]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ASSET_WRAP)
    .setValue(wrap
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .addAllCoins(coins.map(_.toProtoCoin).asJava)
      .build().toByteString)
    .build()

  def getUnwrapTokenMsg(fromAddress: String, fromID: IdentityID, coins: Seq[Coin]): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ASSET_UNWRAP)
    .setValue(unwrap
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .addAllCoins(coins.map(_.toProtoCoin).asJava)
      .build().toByteString)
    .build()

  def getPutOrderMsg(fromAddress: String, fromID: IdentityID, makerAssetID: AssetID, makerSplit: NumberData, expiryHeight: Long, takerAssetID: AssetID, takerSplit: NumberData): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ORDER_PUT)
    .setValue(put
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setMakerSplit(makerSplit.value.toString())
      .setExpiryHeight(Height(expiryHeight).asProtoHeight)
      .setMakerAssetID(makerAssetID.asProtoAssetID)
      .setTakerAssetID(takerAssetID.asProtoAssetID)
      .setTakerSplit(takerSplit.value.toString())
      .build().toByteString)
    .build()

  def getGetOrderMsg(fromAddress: String, fromID: IdentityID, orderID: OrderID): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ORDER_GET)
    .setValue(get
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setOrderID(orderID.asProtoOrderID)
      .build().toByteString)
    .build()

  def getCancelOrderMsg(fromAddress: String, fromID: IdentityID, orderID: OrderID): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ORDER_CANCEL)
    .setValue(orderCancel
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setOrderID(orderID.asProtoOrderID)
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

  def getAssetSendMsg(fromID: IdentityID, fromAddress: String, toID: IdentityID, assetId: AssetID, amount: BigInt): protoBufAny = protoBufAny.newBuilder()
    .setTypeUrl(schema.constants.Messages.ASSET_SEND)
    .setValue(assetSend
      .Message.newBuilder()
      .setFrom(fromAddress)
      .setFromID(fromID.asProtoIdentityID)
      .setToID(toID.asProtoIdentityID)
      .setAssetID(assetId.asProtoAssetID)
      .setValue(amount.toString())
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
