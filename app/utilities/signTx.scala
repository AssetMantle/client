package utilities

import java.security.{KeyPair, Signature, _}
import java.util.Base64

import play.api.libs.json.Json
import queries.responses.common.Account.SinglePublicKey
import scorex.crypto.hash.Sha256
import scorex.crypto.signatures.PublicKey
import org.bitcoinj.signers.TransactionSigner
import org.bitcoinj.crypto.TransactionSignature
import transactions.common.sign
import org.bitcoinj.signers.LocalTransactionSigner
//import  org.bouncycastle.crypto.signers.
import transactions.common.sign.{SignMeta, Signature, StdSignMsg, StdTx, Tx}
import java.io._
import java.security.

object signTx{

  def signTransaction(tx:Tx, meta: SignMeta, key: Key)={

    val signedMsg=createSignMsg(tx, meta)
    val signature=Signature.getInstance("SHA1withDSA", "SUN")


    val x=KeyPair





  }

  def createSignMsg(tx:Tx, meta: SignMeta): StdSignMsg={
    StdSignMsg(meta.account_number, meta.chain_id, tx.fee, tx.memo,tx.msg,meta.sequence)
  }

  def createSignature(signMsg: StdSignMsg, key: Key)={
    val signatureBytes= createSignatureBytes(signMsg, key)

    Signature(
      Base64.getUrlEncoder.encodeToString(signatureBytes),
      SinglePublicKey("tendermint/PubKeySecp256k1",Base64.getUrlEncoder.encodeToString(key.publicKey))
    )
  }

  def createSignatureBytes(signMsg: StdSignMsg, key: Key): Array[Byte]={
      val bytes = toCanonicalJSONBytes(signMsg)

      sign(bytes,key)

  }

  def sign(bytes:Array[Byte], key: Key)={

    val hash = Sha256.hash(bytes)
    val keygen = KeyPairGenerator.getInstance("DSA", "SUN")

    val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
    keygen.initialize(1024, random)

    //val pair =

    val ecdsa =Signature.getInstance("SHA256withECDSA")

    val pv= scorex.crypto.signatures.PrivateKey
    val pubKey:java.security.PublicKey = key.publicKey
    val zxc= PrivateKey.get
    ecdsa.initSign(PublicKey)
    ecdsa.initSign(key.privateKey)
    ecdsa.initSign(key.privateKey)
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