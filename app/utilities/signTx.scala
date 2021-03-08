package utilities

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.spec.{ECGenParameterSpec, ECParameterSpec, ECPoint, ECPrivateKeySpec, ECPublicKeySpec, PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyPair, Signature, _}
import java.util
import java.util.Base64

import play.api.libs.json.Json
import queries.responses.common.Account.SinglePublicKey
import org.bitcoinj
import org.bitcoinj.core.ECKey
import scorex.crypto.hash.Sha256
import scorex.crypto.signatures.PublicKey
import org.bitcoinj.signers.TransactionSigner
import org.bitcoinj.crypto.TransactionSignature
import transactions.common.sign
import org.bitcoinj.signers.LocalTransactionSigner
import org.bitcoinj.signers.LocalTransactionSigner
import org.bitcoinj.signers.LocalTransactionSigner
import org.bouncycastle.asn1.nist.NISTNamedCurves
import org.bouncycastle.crypto.signers
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
//import org.bitcoinj.
//import  org.bouncycastle.crypto.signers.

import org.bitcoinj.crypto.TransactionSignature

import org.bouncycastle.crypto.signers.ECDSASigner

import org.bouncycastle.crypto.signers.ECDSASigner
import transactions.common.sign.{SignMeta, Signature2, StdSignMsg, StdTx, Tx}

import org.bitcoinj.core.TransactionBroadcast
import java.io._
import java.security._

object signTx{

  def signTransaction(tx:Tx, meta: SignMeta, key: ECKey)={

    /*val str = new String(key.publicKey, StandardCharsets.UTF_8)
    val str2 = new String(key.privateKey, StandardCharsets.UTF_8)
    println(str)
    println(str2)*/


    val signedMsg=createSignMsg(tx, meta)
    //val signature=Signature.getInstance("SHA1withDSA", "SUN")
    createSignature(signedMsg, key)
      val ecsaSigner= new ECDSASigner()
    //ecsaSigner.init()
    //ecsaSigner.generateSignature()
    //val x=KeyPair

  }

  def createSignMsg(tx:Tx, meta: SignMeta): StdSignMsg={
    StdSignMsg(meta.account_number, meta.chain_id, tx.fee, tx.memo,tx.msg,meta.sequence)
  }

  def createSignature(signMsg: StdSignMsg, key: ECKey)={
    val signatureBytes= createSignatureBytes(signMsg, key)

    /*Signature2(
      Base64.getUrlEncoder.encodeToString(signatureBytes),
      SinglePublicKey("tendermint/PubKeySecp256k1",Base64.getUrlEncoder.encodeToString(key.publicKey))
    )*/
  }

  def createSignatureBytes(signMsg: StdSignMsg, key: ECKey)={
      val jsonString = toCanonicalJSONBytes(signMsg)
    println(jsonString)
    val xsignature=key.signMessage(jsonString)

    println(key.getPubKey.mkString)
    println(key.getPubKeyPoint.toString)
    println(key.getPubKeyHash.mkString)
    println(key.getPublicKeyAsHex)
    println(Base64.getEncoder.encodeToString(key.getPubKey))

    println("signature--"+xsignature)
   val bool= try {
      key.verifyMessage(jsonString, xsignature)
      true
    }
    catch {
      case exception: Exception=> false
    }

    println("asdasdBoolBitcoinj--"+bool)
      //sign(bytes,key)

  }

  def sign(bytes:Array[Byte], key: Key)={

   /* val hash = Sha256.hash(bytes)
    val keygen = KeyPairGenerator.getInstance("DSA", "SUN")

    val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
    keygen.initialize(1024, random)*/

    //val pair =

    val ecdsa =Signature.getInstance("SHA256withECDSA")

   // val pv= scorex.crypto.signatures.PrivateKey
    //val pubKey:java.security.PublicKey = key.publicKey

    //val privateKey = key.privateKey.to

    //val ECPRIVATEkey = new ECPrivateKeySpec("","")

   // val m = MessageDigest.getInstance("MD5")
   // m.update(message.getBytes, 0, message.length)
    //val bi = new BigInt(1, m.digest)

   // val x= new BigInteger(1, privateKeyBytes)

   // val spec2= new ECParameterSpec()

   // val ass= ECNamedCurveTable.getParameterSpec("")

    val parameters = AlgorithmParameters.getInstance("EC")
    parameters.init(new ECGenParameterSpec("secp256k1"))
    val ecParameterSpec= parameters.getParameterSpec(classOf[ECParameterSpec])

    val ecPoint = new ECPoint(new BigInteger(1, util.Arrays.copyOfRange(key.publicKey, 0, 24)),new BigInteger(1, util.Arrays.copyOfRange(key.publicKey, 24, 48)))
    //val w= getDecoded()
     //val ecParams= NISTNamedCurves.getByName("secp256k1")
    // val ecCurveSpec= new ECNamedCurveSpec("secp256k1", ecParams.getCurve, ecParams.getG, ecParams.getN)
    val fromateddPriavteKey= new ECPrivateKeySpec(new BigInteger(key.privateKey), ecParameterSpec)
    val formattedPubKey = new ECPublicKeySpec(ecPoint , ecParameterSpec)

    val kf= KeyFactory.getInstance("EC")

    val priv= kf.generatePrivate(fromateddPriavteKey)
    val pubKey = kf.generatePublic(formattedPubKey)

    ecdsa.initSign(priv)
    ecdsa.update(bytes)
    val signature=ecdsa.sign()

    ecdsa.initVerify(pubKey)
    ecdsa.update(bytes)
    val bool= ecdsa.verify(signature)

    println("verified---"+bool)
    signature


    val ecSpec= new ECGenParameterSpec("secp256k1")
    val generator = KeyPairGenerator.getInstance("EC")

    generator.initialize(ecSpec,new SecureRandom())
    val keyPair = generator.generateKeyPair()
    val publicKey = keyPair.getPublic()
    val privateKey= keyPair.getPrivate

    val ecdsa2 =Signature.getInstance("SHA256withECDSA")
    ecdsa2.initSign(privateKey)
    ecdsa2.update(bytes)
    val signature2=ecdsa2.sign()

      ecdsa2.initVerify(publicKey)
    ecdsa2.update(bytes)
    val boolRes= ecdsa2.verify(signature2)
    println(boolRes)
    //val zxc= PrivateKey.get
    /*ecdsa.initSign(PublicKey)
    ecdsa.initSign(key.privateKey)
    ecdsa.initSign(key.privateKey)*/
   // ecdsa.si
    signature
  }


  def toCanonicalJSONBytes(signMsg: StdSignMsg): String={

    val z=Json.toJson(signMsg)


    Json.toJson(signMsg).toString()

  }

  def toCanonicalJSONString(signMsg: StdSignMsg)={

  }

  def toCanonicalJSON(signMsg: StdSignMsg)={

    val y= signMsg.getClass.getDeclaredFields.map(_.getName).zip(signMsg.productIterator.to).toMap

    val keys = signMsg.getClass.getDeclaredFields.map(_.getName)
    val values = signMsg.productIterator.to
  }

}