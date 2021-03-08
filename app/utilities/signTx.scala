package utilities

import java.security.spec.{ECPrivateKeySpec, ECPublicKeySpec, PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyPair, Signature, _}
import java.util.Base64

import play.api.libs.json.Json
import queries.responses.common.Account.SinglePublicKey
import org.bitcoinj
import scorex.crypto.hash.Sha256
import scorex.crypto.signatures.PublicKey
import org.bitcoinj.signers.TransactionSigner
import org.bitcoinj.crypto.TransactionSignature
import transactions.common.sign
import org.bitcoinj.signers.LocalTransactionSigner
import org.bitcoinj.signers.LocalTransactionSigner

import org.bitcoinj.

import org.bouncycastle.crypto.signers

import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import org.bitcoinj.

//import  org.bouncycastle.crypto.signers.
import org.bouncycastle.crypto.signers.ECDSASigner
import transactions.common.sign.{SignMeta, Signature2, StdSignMsg, StdTx, Tx}
import org.bitcoinj.core.TransactionBroadcast
import java.io._
import java.security._

object signTx{

  def signTransaction(tx:Tx, meta: SignMeta, key: Key)={

    val signedMsg=createSignMsg(tx, meta)
    //val signature=Signature.getInstance("SHA1withDSA", "SUN")
    createSignature(signedMsg, key)
      val ecsaSigner= new ECDSASigner()
    ecsaSigner.init()
    ecsaSigner.generateSignature()
    //val x=KeyPair

  }

  def createSignMsg(tx:Tx, meta: SignMeta): StdSignMsg={
    StdSignMsg(meta.account_number, meta.chain_id, tx.fee, tx.memo,tx.msg,meta.sequence)
  }

  def createSignature(signMsg: StdSignMsg, key: Key)={
    val signatureBytes= createSignatureBytes(signMsg, key)

    Signature2(
      Base64.getUrlEncoder.encodeToString(signatureBytes),
      SinglePublicKey("tendermint/PubKeySecp256k1",Base64.getUrlEncoder.encodeToString(key.publicKey))
    )
  }

  def createSignatureBytes(signMsg: StdSignMsg, key: Key): Array[Byte]={
      val bytes = toCanonicalJSONBytes(signMsg)

      sign(bytes,key)

  }

  def sign(bytes:Array[Byte], key: Key)={

   /* val hash = Sha256.hash(bytes)
    val keygen = KeyPairGenerator.getInstance("DSA", "SUN")

    val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
    keygen.initialize(1024, random)*/

    //val pair =

    val ecdsa =Signature.getInstance("SHA256withECDSA")

    val pv= scorex.crypto.signatures.PrivateKey
    //val pubKey:java.security.PublicKey = key.publicKey

    //val privateKey = key.privateKey.to

    val ECPRIVATEkey = new ECPrivateKeySpec("","")
    val fromateddPriavteKey= new PKCS8EncodedKeySpec(key.privateKey)
    val formattedPubKey = new X509EncodedKeySpec(key.publicKey)
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

    //val zxc= PrivateKey.get
    /*ecdsa.initSign(PublicKey)
    ecdsa.initSign(key.privateKey)
    ecdsa.initSign(key.privateKey)*/
   // ecdsa.si

  }


  def toCanonicalJSONBytes(signMsg: StdSignMsg): Array[Byte]={
    Json.toJson(signMsg).toString().getBytes()
  }

  def toCanonicalJSONString(signMsg: StdSignMsg)={

  }

  def toCanonicalJSON(signMsg: StdSignMsg)={

    val y= signMsg.getClass.getDeclaredFields.map(_.getName).zip(signMsg.productIterator.to).toMap

    val keys = signMsg.getClass.getDeclaredFields.map(_.getName)
    val values = signMsg.productIterator.to



  }

}