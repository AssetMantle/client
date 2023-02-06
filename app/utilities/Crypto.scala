package utilities

import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.bits.ByteVector

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

}
