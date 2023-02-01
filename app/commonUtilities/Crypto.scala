package commonUtilities

import commonUtilities.Wallet.BouncyHash

import java.security.MessageDigest
import java.util.Base64

object Crypto {

  def convertConsensusAddressToHexAddress(consKey: String): String = Bech32.decodeBech32(consKey)._2.toUpperCase

  def convertConsensusPubKeyToHexAddress(consensusPubKey: String): String = {
    val pubKeyBytes = Bech32.decodeBech32(consensusPubKey)._2.splitAt(10)._2.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
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

  def convertAccountPublicKeyToAccountAddress(pubkey: String): String = Bech32.encode(commonConstants.Blockchain.AccountPrefix, commonUtilities.Bech32.to5Bit(BouncyHash.ripemd160.digest(MessageDigest.getInstance("SHA-256").digest(Base64.getUrlDecoder.decode(pubkey.replace("+", "-").replace("/", "_"))))))

  def convertAccountAddressBytesToBech32Address(value: Array[Byte]): String = Bech32.encode(commonConstants.Blockchain.AccountPrefix, commonUtilities.Bech32.to5Bit(BouncyHash.ripemd160.digest(value)))

  def convertAccountAddressToOperatorAddress(accountAddress: String, hrp: String = commonConstants.Blockchain.ValidatorPrefix): String = {
    val byteSeq = Bech32.decode(accountAddress)._2
    Bech32.encode(hrp, byteSeq)
  }

  //probably byteSeq converts operatorAddress to hexAddress and then encode converts into wallet address
  def convertOperatorAddressToAccountAddress(operatorAddress: String, hrp: String = commonConstants.Blockchain.AccountPrefix): String = {
    val byteSeq = Bech32.decode(operatorAddress)._2
    Bech32.encode(hrp, byteSeq)
  }

  def pubKeyToBech32(pubKey: String): String = Bech32.convertAndEncode(commonConstants.Blockchain.ValidatorConsensusPublicPrefix, "1624DE6420" + pubKey)

}
