package utilities

import com.cosmos.authz.{v1beta1 => authzTx}
import com.cosmos.bank.{v1beta1 => bankTx}
import com.cosmos.crisis.{v1beta1 => crisisTx}
import com.cosmos.distribution.{v1beta1 => distributionTx}
import com.cosmos.evidence.{v1beta1 => evidenceTx}
import com.cosmos.feegrant.{v1beta1 => feegrantTx}
import com.cosmos.gov.{v1beta1 => govTx}
import com.cosmos.slashing.{v1beta1 => slashingTx}
import com.cosmos.staking.{v1beta1 => stakingTx}
import com.cosmos.vesting.{v1beta1 => VestingTx}
import exceptions.BaseException
import com.ibc.applications.transfer.{v1 => transferTx}
import com.ibc.core.channel.{v1 => channelTx}
import com.ibc.core.client.{v1 => clientTx}
import com.ibc.core.connection.{v1 => connectionTx}
import models.Abstract.{Authorization, FeeAllowance}
import models.blockchain.Transaction
import models.common.Serializable.Coin
import models.masterTransaction.WalletTransaction
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.{ECDomainParameters, ECPublicKeyParameters}
import org.bouncycastle.crypto.signers.ECDSASigner
import play.api.Logger
import utilities.Date.RFC3339

import java.math.BigInteger
import scala.jdk.CollectionConverters.CollectionHasAsScala

object Blockchain {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_BLOCKCHAIN

  case class AccountTransaction(address: String, txHash: String) {
    def toWalletTx(height: Int): WalletTransaction = WalletTransaction(address = this.address, txHash = this.txHash, height = height)
  }

  case class ValidatorTransaction(address: String, txHash: String) {
    def toValidatorTx(height: Int): models.masterTransaction.ValidatorTransaction = models.masterTransaction.ValidatorTransaction(address = this.address, txHash = this.txHash, height = height)
  }

  def addCoins(oldCoins: Seq[Coin], add: Seq[Coin]): Seq[Coin] = if (oldCoins.nonEmpty) {
    val newCoins = oldCoins.map(oldCoin => add.find(_.denom == oldCoin.denom).fold(oldCoin)(addCoin => Coin(denom = addCoin.denom, amount = oldCoin.amount + addCoin.amount)))
    newCoins ++ add.filter(x => !newCoins.map(_.denom).contains(x.denom))
  } else add

  def subtractCoins(fromCoins: Seq[Coin], amount: Seq[Coin]): (Seq[Coin], Boolean) = {
    val result = addCoins(fromCoins, amount.map(x => x.copy(amount = x.amount * -1)))
    (result, result.exists(_.isNegative == true))
  }

  object Authz {
    case class ValidateResponse(accept: Boolean, delete: Boolean, updated: Option[Authorization])
  }

  object FeeGrant {
    case class ValidateResponse(delete: Boolean, updated: FeeAllowance)
  }

  case class SlashingEvidence(height: Int, time: RFC3339, validatorHexAddress: String, validatorPower: MicroNumber)

  def verifySecp256k1Signature(publicKey: String, data: Array[Byte], signature: String): Boolean = verifySecp256k1Signature(publicKey = utilities.Secrets.base64Decoder(publicKey), data, signature = utilities.Secrets.base64Decoder(signature))

  def verifySecp256k1Signature(publicKey: Array[Byte], data: Array[Byte], signature: Array[Byte]): Boolean = {
    try {
      if (signature.length != 64) {
        throw new BaseException(constants.Response.INVALID_SIGNATURE)
      }
      val signer = new ECDSASigner()
      val params = SECNamedCurves.getByName("secp256k1")
      val ecParams = new ECDomainParameters(params.getCurve, params.getG, params.getN, params.getH)
      val ecPoint = ecParams.getCurve.decodePoint(publicKey)
      val pubKeyParams = new ECPublicKeyParameters(ecPoint, ecParams)
      signer.init(false, pubKeyParams)
      signer.verifySignature(data, getR(signature), getS(signature))
    } catch {
      case baseException: BaseException => throw baseException
      case exception: Exception => throw new BaseException(constants.Response.INVALID_SIGNATURE, exception)
    }
  }

  private def getR(signature: Array[Byte]): BigInteger = {
    if (signature.length != 64) throw new BaseException(constants.Response.INVALID_SIGNATURE)
    else {
      val r = signature.take(32)
      val finalR = if (r(0) <= 0) 0.toByte +: r else r
      new BigInteger(finalR)
    }
  }

  private def getS(signature: Array[Byte]): BigInteger = {
    if (signature.length != 64) throw new BaseException(constants.Response.INVALID_SIGNATURE)
    else {
      val s = signature.takeRight(32)
      val finalS = if (s(0) <= 0) 0.toByte +: s else s
      new BigInteger(finalS)
    }
  }



  def getValidatorTxs(transaction: Transaction): Seq[ValidatorTransaction] = {
    val addresses = transaction.getMessages.map { stdMsg =>
      stdMsg.getTypeUrl match {
        case schema.constants.Messages.WITHDRAW_VALIDATOR_COMMISSION => distributionTx.MsgWithdrawValidatorCommission.parseFrom(stdMsg.getValue).getValidatorAddress
        case schema.constants.Messages.UNJAIL => slashingTx.MsgUnjail.parseFrom(stdMsg.getValue).getValidatorAddr
        case schema.constants.Messages.CREATE_VALIDATOR => stakingTx.MsgCreateValidator.parseFrom(stdMsg.getValue).getValidatorAddress
        case schema.constants.Messages.EDIT_VALIDATOR => stakingTx.MsgEditValidator.parseFrom(stdMsg.getValue).getValidatorAddress
      }
    }
    addresses.distinct.map(x => ValidatorTransaction(address = x, txHash = transaction.hash))
  }


}
