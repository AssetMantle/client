package commonUtilities

import commonConstants.Bip39.WordList
import scodec.bits.BitVector
import scorex.crypto.hash.Sha256

import java.security.SecureRandom
import java.text.Normalizer.Form.NFKD
import java.text.Normalizer.normalize
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Bip39 {
  //Taken help from https://github.com/AlanVerbner/bip39
  //https://github.com/AlanVerbner/bip39/blob/master/src/main/scala/com/github/alanverbner/bip39/package.scala

  private val WordListSize = 2048
  private val BitsGroupSize = 11
  private val Pbkdf2Algorithm = "PBKDF2WithHmacSHA512"
  private val Pbkdf2Iterations = 2048
  private val Pbkdf2KeyLength = 512
  private val AllowedEntropyByteSizes: Seq[Int] = Seq(Entropy128, Entropy160, Entropy192, Entropy224, Entropy256).map(_.bits / 8)

  private def generate(entropy: Entropy, wordList: WordList, secureRandom: SecureRandom): String = {
    require(wordList.words.length == WordListSize, s"WordList should have $WordListSize words")
    val ent: Array[Byte] = BitVector.fill(entropy.bits)(high = false).toByteArray
    secureRandom.nextBytes(ent)
    generate(ent, wordList)
  }

  private def generate(ent: Array[Byte], wordList: WordList): String = {
    require(AllowedEntropyByteSizes contains ent.length, "Entropy size should be 32, 40, 48, 56 or 64")
    val checksum = BitVector(Sha256.hash(ent))
    val entWithChecksum = BitVector(ent) ++ checksum.take(ent.length / 4)
    entWithChecksum.grouped(BitsGroupSize).map { wordIndex =>
      wordList.words(wordIndex.toInt(signed = false))
    } mkString wordList.delimiter
  }

  def validate(mnemonics: Seq[String], wordList: WordList = commonConstants.Bip39.EnglishWordList): Boolean = {
    require(wordList.words.size == WordListSize, s"WordList should have $WordListSize words")
    if (mnemonics.length % 3 != 0) false
    else {
      val entWithChecksum = mnemonics
        .map(normalize(_, NFKD))
        .map(wordList.words.indexOf(_)).foldLeft(BitVector.empty) { (ent: BitVector, wordIndex: Int) =>
        ent ++ BitVector.fromInt(wordIndex, size = BitsGroupSize)
      }
      val checkSumSize = entWithChecksum.length / 33
      val ent = entWithChecksum.dropRight(checkSumSize)
      val checksum = entWithChecksum.takeRight(checkSumSize)
      checksum == BitVector(Sha256.hash(ent.toByteArray)).take(checkSumSize)
    }
  }

  def toSeed(mnemonic: String, passphrase: Option[String] = None): Array[Byte] = {
    val spec = new PBEKeySpec(
      normalize(mnemonic, NFKD).toCharArray,
      normalize(s"mnemonic${passphrase.getOrElse("")}", NFKD).getBytes,
      Pbkdf2Iterations,
      Pbkdf2KeyLength
    )
    val skf = SecretKeyFactory.getInstance(Pbkdf2Algorithm)
    skf.generateSecret(spec).getEncoded
  }

  sealed abstract class Entropy(val bits: Int)

  //Generates 12 words mnemonic
  case object Entropy128 extends Entropy(128)

  //Generates 15 words mnemonic
  case object Entropy160 extends Entropy(160)

  //Generates 18 words mnemonic
  case object Entropy192 extends Entropy(192)

  //Generates 21 words mnemonic
  case object Entropy224 extends Entropy(224)

  //Generates 24 words mnemonic
  case object Entropy256 extends Entropy(256)

  def creatRandomMnemonics(entropy: Entropy = Entropy256): Seq[String] = generate(entropy, commonConstants.Bip39.EnglishWordList, new SecureRandom()).split(" ")

}
