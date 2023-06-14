package utilities

import org.bitcoinj.core.ECKey
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.{ECDomainParameters, ECPublicKeyParameters}
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.jce.provider.BouncyCastleProvider
import play.api.Logger
import scodec.bits.ByteVector

import java.math.BigInteger
import java.security.{MessageDigest, Security}
import java.util.Base64

object Crypto {

  object BouncyHash {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider())
    }

    def sha256: MessageDigest = MessageDigest.getInstance("SHA-256", "BC")

    def ripemd160: MessageDigest = MessageDigest.getInstance("RipeMD160", "BC")

    def kec256(bytes: ByteVector): Array[Byte] = new Keccak.Digest256().digest(bytes.toArray)

    def kec512(bytes: ByteVector): Array[Byte] = new Keccak.Digest512().digest(bytes.toArray)
  }


  def convertConsensusAddressToHexAddress(consKey: String): String = utilities.Bech32.decodeBech32(consKey)._2.toUpperCase

  def convertConsensusPubKeyToHexAddress(consensusPubKey: String): String = {
    val pubKeyBytes = utilities.Bech32.decodeBech32(consensusPubKey)._2.splitAt(10)._2.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
    MessageDigest.getInstance("SHA-256").digest(pubKeyBytes).slice(0, 20).map("%02x".format(_)).mkString.toUpperCase
  }

  def convertValidatorPublicKeyToHexAddress(pubkey: String): String = {
    MessageDigest
      .getInstance("SHA-256")
      .digest(Base64.getUrlDecoder.decode(pubkey.replace("+", "-").replace("/", "_")))
      .slice(0, 20)
      .map("%02x".format(_))
      .mkString.toUpperCase
  }

  def convertAccountPublicKeyToAccountAddress(pubkey: String): String = utilities.Bech32.encode(constants.Blockchain.AccountPrefix, utilities.Bech32.to5Bit(BouncyHash.ripemd160.digest(MessageDigest.getInstance("SHA-256").digest(Base64.getUrlDecoder.decode(pubkey.replace("+", "-").replace("/", "_"))))))

  def convertAccountPublicKeyToAccountAddress(pubkey: Array[Byte]): String = utilities.Bech32.encode(constants.Blockchain.AccountPrefix, utilities.Bech32.to5Bit(BouncyHash.ripemd160.digest(MessageDigest.getInstance("SHA-256").digest(pubkey))))

  def convertAddressToAccAddressBytes(address: String): Array[Byte] = utilities.Bech32.from5Bit(utilities.Bech32.decode(address)._2).toArray

  def convertAccAddressBytesToAddress(addressBytes: Array[Byte]): String = utilities.Bech32.encode(constants.Blockchain.AccountPrefix, utilities.Bech32.to5Bit(addressBytes))

  def convertAccountAddressToOperatorAddress(accountAddress: String, hrp: String = constants.Blockchain.ValidatorPrefix): String = {
    val byteSeq = utilities.Bech32.decode(accountAddress)._2
    utilities.Bech32.encode(hrp, byteSeq)
  }

  //probably byteSeq converts operatorAddress to hexAddress and then encode converts into wallet address
  def convertOperatorAddressToAccountAddress(operatorAddress: String, hrp: String = constants.Blockchain.AccountPrefix): String = {
    val byteSeq = utilities.Bech32.decode(operatorAddress)._2
    utilities.Bech32.encode(hrp, byteSeq)
  }

  def pubKeyToBech32(pubKey: String): String = utilities.Bech32.convertAndEncode(constants.Blockchain.ValidatorConsensusPublicPrefix, "1624DE6420" + pubKey)

  def validateSignature(data: Array[Byte], signature: Array[Byte], publicKey: Array[Byte])(implicit module: String, logger: Logger): Boolean = try {
    ECKey.verify(data, signature, publicKey)
  } catch {
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      false
  }

  def verifySecp256k1Signature(publicKey: String, data: Array[Byte], signature: String)(implicit module: String, logger: Logger): Boolean = verifySecp256k1Signature(publicKey = utilities.Secrets.base64Decoder(publicKey), data, signature = utilities.Secrets.base64Decoder(signature))

  def verifySecp256k1Signature(publicKey: Array[Byte], data: Array[Byte], signature: Array[Byte])(implicit module: String, logger: Logger): Boolean = {
    try {
      if (signature.length != 64) {
        constants.Response.INVALID_SIGNATURE.throwBaseException()
      }
      val signer = new ECDSASigner()
      val params = SECNamedCurves.getByName("secp256k1")
      val ecParams = new ECDomainParameters(params.getCurve, params.getG, params.getN, params.getH)
      val ecPoint = ecParams.getCurve.decodePoint(publicKey)
      val pubKeyParams = new ECPublicKeyParameters(ecPoint, ecParams)
      signer.init(false, pubKeyParams)
      signer.verifySignature(data, getR(signature), getS(signature))
    } catch {
      case exception: Exception => constants.Response.INVALID_SIGNATURE.throwBaseException(exception)
    }
  }

  private def getR(signature: Array[Byte])(implicit module: String, logger: Logger): BigInteger = {
    if (signature.length != 64) constants.Response.INVALID_SIGNATURE.throwBaseException()
    else {
      val r = signature.take(32)
      val finalR = if (r(0) <= 0) 0.toByte +: r else r
      new BigInteger(finalR)
    }
  }

  private def getS(signature: Array[Byte])(implicit module: String, logger: Logger): BigInteger = {
    if (signature.length != 64) constants.Response.INVALID_SIGNATURE.throwBaseException()
    else {
      val s = signature.takeRight(32)
      val finalS = if (s(0) <= 0) 0.toByte +: s else s
      new BigInteger(finalS)
    }
  }

}
