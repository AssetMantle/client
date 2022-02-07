package utilities

import exceptions.BaseException
import models.Abstract.Authz.Authorization
import models.common.FeeGrant.Allowance
import models.common.Serializable.Coin
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.{ECDomainParameters, ECPublicKeyParameters}
import org.bouncycastle.crypto.signers.ECDSASigner
import play.api.Logger
import utilities.Date.RFC3339

import java.math.BigInteger

object Blockchain {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_BLOCKCHAIN

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
    case class ValidateResponse(delete: Boolean, updated: Allowance)
  }

  case class SlashingEvidence(height: Int, time: RFC3339, validatorHexAddress: String, validatorPower: MicroNumber)

  def verifySecp256k1Signature(publicKey: String, data: Array[Byte], signature: String): Boolean = verifySecp256k1Signature(publicKey = utilities.Hash.base64Decoder(publicKey), data, signature = utilities.Hash.base64Decoder(signature))

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
}
