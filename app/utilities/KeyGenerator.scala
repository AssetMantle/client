package utilities

import com.google.common.collect
import com.google.common.collect.ImmutableList
import exceptions.BaseException
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.Script
import org.bitcoinj.wallet.{DeterministicSeed, Wallet}
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.jce.provider.BouncyCastleProvider
import play.api.Logger
import scodec.bits.ByteVector
import scorex.crypto.signatures.Signature

import java.security.{MessageDigest, Security}
import scala.util.{Failure, Success}

case class Key(address: String, hdPath: String, publicKey: Array[Byte], privateKey: Array[Byte], mnemonics: Seq[String])

object KeyGenerator {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_KEY_GENERATOR

  object BouncyHash {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider())
    }

    def genInstance(algorithm: String): MessageDigest = MessageDigest.getInstance(algorithm, "BC")

    val sha256: MessageDigest = genInstance("SHA-256")
    val ripemd160: MessageDigest = genInstance("RipeMD160")

    def kec256(bytes: ByteVector): Array[Byte] = new Keccak.Digest256().digest(bytes.toArray)

    def kec512(bytes: ByteVector): Array[Byte] = new Keccak.Digest512().digest(bytes.toArray)
  }

  def getHDPath(account: Int, addressIndex: Int): ImmutableList[ChildNumber] = {
    collect.ImmutableList.of(
      new ChildNumber(44, true),
      new ChildNumber(constants.Blockchain.CoinType, true),
      new ChildNumber(account, true),
      new ChildNumber(0, false),
      new ChildNumber(addressIndex, false)
    )
  }

  def getKey(mnemonics: Seq[String], hdPath: ImmutableList[ChildNumber] = constants.Blockchain.DefaultHDPath, bip39Passphrase: Option[String] = None) = {
    val words = mnemonics.mkString(" ")
    if (Bip39.validate(words)) {
      val wallet = Wallet.fromSeed(
        MainNetParams.get(),
        new DeterministicSeed(words, Bip39.toSeed(words, bip39Passphrase), "", System.currentTimeMillis()),
        Script.ScriptType.P2PKH,
        hdPath
      )


      utilities.Bech32.encode(constants.Blockchain.AccountPrefix, utilities.Bech32.to5Bit(BouncyHash.ripemd160.digest(MessageDigest.getInstance("SHA-256").digest(wallet.getKeyByPath(hdPath).getPubKey)))) match {
        case Success(address) => {
          Key(address = address, hdPath = hdPath.toString, publicKey = wallet.getKeyByPath(hdPath).getPubKey, privateKey = wallet.getKeyByPath(hdPath).getPrivKeyBytes, mnemonics = mnemonics)}
          wallet.getKeyByPath(hdPath)
        case Failure(exception) => logger.error(exception.getLocalizedMessage)
          throw new BaseException(constants.Response.KEY_GENERATION_FAILED)
      }
    } else throw new BaseException(constants.Response.INVALID_MNEMONICS)
  }

}
