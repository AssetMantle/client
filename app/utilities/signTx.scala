package utilities

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.interfaces.ECPrivateKey
import java.security.spec.{ECGenParameterSpec, ECParameterSpec, ECPoint, ECPrivateKeySpec, ECPublicKeySpec, PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyPair, Signature, _}
import java.util
import java.util.Base64

import org.bouncycastle.util.encoders.{ Hex, Base64 => BouncyCastleBAse64}
import javax.xml.bind.DatatypeConverter
import play.api.libs.json.Json
import queries.responses.common.Account.SinglePublicKey
import org.bitcoinj
import org.bitcoinj.core.ECKey.ECDSASignature
import org.bitcoinj.core.{ECKey, Sha256Hash}
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

import java.security.{MessageDigest, Security}
import org.bouncycastle.crypto.signers.ECDSASigner
import transactions.common.sign.{SignMeta, Signature2, StdSignMsg, StdTx, Tx}

import org.bitcoinj.core.TransactionBroadcast
import java.io._


import org.web3j.crypto._
import org.web3j.crypto.{Hash => Web3jHash}


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
    println(jsonString.getBytes().length)
    println(jsonString.getBytes().toList)

    //println(sonString.getBytes())

    //val hash = Sha256.hash(jsonString.getBytes()).

     val hash=  MessageDigest.getInstance("SHA-256").digest(jsonString.getBytes("UTF-8"))



    println("hash--"+hash)

    val byteArrayHash= MessageDigest
      .getInstance("SHA-256")
      .digest(jsonString.getBytes("UTF-8"))

    println("byteArrayHash--"+byteArrayHash.toList)

    //val sha256Hash= Sha256Hash.wrap(byteArrayHash)

    val sha256Hash= Sha256Hash.of(hash)

    val hash2= DatatypeConverter.printHexBinary(
      MessageDigest
        .getInstance("SHA-256")
        .digest(jsonString.getBytes("UTF-8")))

    println(hash2)

   // key.sign()
    val xsignature=key.signMessage(jsonString)


    val byteArray = jsonString.getBytes()






    val anotherSignature = key.sign(sha256Hash)
    println("anotherSignature.r--"+anotherSignature.r)
    println("anotherSignature.s---"+anotherSignature.s)

    println("anotherSignature.r---ByteArray--"+anotherSignature.r.toByteArray)
    println("anotherSignature.s----ByteArray---"+anotherSignature.s.toByteArray)

    println("anotherSignature.r---Base64--"+Base64.getEncoder.encodeToString(anotherSignature.r.toByteArray))
    println("anotherSignature.s----Base64-"+Base64.getEncoder.encodeToString(anotherSignature.s.toByteArray))

    println(key.getPublicKeyAsHex)
    println(Base64.getEncoder.encodeToString(key.getPubKey))
    println(Base64.getEncoder.encodeToString(key.getPrivKeyBytes))

    println("signature--"+xsignature)
   val bool= try {
      key.verifyMessage(jsonString, xsignature)
      true
    }
    catch {
      case exception: Exception=> false
    }

    println("asdasdBoolBitcoinj--"+bool)
      sign(jsonString.getBytes("UTF-8"),key)

  }

  def sign(bytes:Array[Byte], key: ECKey)={

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

    val ecPoint = key.getPubKeyPoint
    //val w= getDecoded()
     //val ecParams= NISTNamedCurves.getByName("secp256k1")
    // val ecCurveSpec= new ECNamedCurveSpec("secp256k1", ecParams.getCurve, ecParams.getG, ecParams.getN)
    val fromateddPriavteKey= new ECPrivateKeySpec(key.getPrivKey, ecParameterSpec)
   // val formattedPubKey = new ECPublicKeySpec(ecPoint , ecParameterSpec)

    val kf= KeyFactory.getInstance("EC")

    val priv= kf.generatePrivate(fromateddPriavteKey)
    //val pubKey = kf.generatePublic(formattedPubKey)

    ecdsa.initSign(priv)
    ecdsa.update(bytes)
    val signature=ecdsa.sign()

    //ecdsa.initVerify(pubKey)
    //ecdsa.update(bytes)
    //val bool= ecdsa.verify(signature)

  //  println("verified---"+bool)
   // Base64.
  //  println("signature222---"+ new String(Base64.getDecoder.decode(signature)))
    signature

    val ecSpec= new ECGenParameterSpec("secp256k1")
    val generator = KeyPairGenerator.getInstance("EC")

    generator.initialize(ecSpec,new SecureRandom())
    val keyPair = generator.generateKeyPair()
    val publicKey = keyPair.getPublic()
    val privateKey= keyPair.getPrivate()

    val ecParams= keyPair.getPrivate().asInstanceOf[ECPrivateKey].getParams
    println(ecParams)

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


  def javaSecuritySigning(tx:Tx, meta: SignMeta, key: ECKey)={
    val signedMsg=createSignMsg(tx, meta)
    val jsonString = toCanonicalJSONBytes(signedMsg)
   // val jsonString = toCanonicalJSONBytes(tx)
    val bytes = jsonString.getBytes(StandardCharsets.UTF_8)


    val ecSpec= new ECGenParameterSpec("secp256k1")
    val generator = KeyPairGenerator.getInstance("EC")

    generator.initialize(ecSpec,new SecureRandom())
    val keyPair = generator.generateKeyPair()
    val publicKey = keyPair.getPublic()
    val privateKey= keyPair.getPrivate()

    val ecParams= keyPair.getPrivate().asInstanceOf[ECPrivateKey].getParams
    println(ecParams)

    val ecPriavetkeySpec= new ECPrivateKeySpec(key.getPrivKey, ecParams)
    val kfac= KeyFactory.getInstance("EC")
    val privKey = kfac.generatePrivate(ecPriavetkeySpec)
    println("privKey--"+privKey.getEncoded.toList.toString)
    println("ECprivKey--"+key.getPrivKeyBytes.toList.toString)
    val bool2 = privKey.getEncoded.sameElements(key.getPrivKeyBytes)

    val xyz = privKey.asInstanceOf[ECPrivateKey].getS
    val zxy = key.getPrivKey
    println("bool2---"+bool2)
    println(xyz)
    println(zxy)
    println("bool3 "+(xyz==zxy))

    val parameters = AlgorithmParameters.getInstance("EC")
    parameters.init(new ECGenParameterSpec("secp256k1"))
    val ecParameterSpec= parameters.getParameterSpec(classOf[ECParameterSpec])
    val ecPoint = key.getPubKeyPoint

    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())

    val ecNamedCurveParameterSpec= ECNamedCurveTable.getParameterSpec("secp256k1")
    val ecPrivateKeySpec= new ECPrivateKeySpec(key.getPrivKey, ecParameterSpec)

   // val x= new EllipticCurve()
    //val ecParams= NISTNamedCurves.getByName("secp256k1")
    // val ecCurveSpec= new ECNamedCurveSpec("secp256k1", ecParams.getCurve, ecParams.getG, ecParams.getN)
    val fromateddPriavteKey= new ECPrivateKeySpec(key.getPrivKey, ecParameterSpec)
    // val formattedPubKey = new ECPublicKeySpec(ecPoint , ecParameterSpec)

    val kf= KeyFactory.getInstance("EC")

    val priv= kf.generatePrivate(fromateddPriavteKey)
    //val pubKey = kf.generatePublic(formattedPubKey)
    val ecdsa =Signature.getInstance("SHA256withECDSA")

    val bool = key.getPrivKeyBytes == priv.getEncoded
    println("same privateKey------"+ bool)
    println("key.getPrivKeyBytes-----"+ key.getPrivKeyBytes.toList.toString())
    println("priv.getEncoded---"+priv.getEncoded.toList.toString())
    //println("priv.getEncodedAnd Decode---"+Base64.getDecoder.decode(priv.getEncoded).toList.toString())

    ecdsa.initSign(priv)
    ecdsa.update(bytes)
    val signature=ecdsa.sign()
    println(signature.toList.toString())
   // println(Base64)
    //val signatureString = new String(BouncyCastleBAse64.decode(signature), StandardCharsets.UTF_8)
    //println(signatureString)
    println("Signature :--"+new BigInteger(1,signature).toString(16))
    println("Signature22 :--"+Base64.getEncoder.encodeToString(new BigInteger(1,signature).toString(16).getBytes()))
    println(Base64.getEncoder.encodeToString(signature))
    println(Base64.getUrlEncoder.encodeToString(signature))

    //val signature2 = Base64.getDecoder.decode(signature)
   // val signatureString2 = new String(signature2, StandardCharsets.UTF_8)
   // println(signatureString2)
    //ecdsa.initVerify(pubKey)
    //ecdsa.update(bytes)
    //val bool= ecdsa.verify(signature)

    //  println("verified---"+bool)

  }


  def bitcoinjSigning(tx:Tx, meta: SignMeta, key: ECKey)={
    val signedMsg = createSignMsg(tx, meta)
    val jsonString = toCanonicalJSONBytes(signedMsg)

    val xsignature=key.signMessage(jsonString)
   // xsi





  }

  def bouncyCastleSigning()={




  }

  def signweb3j(tx:Tx, meta: SignMeta, key: ECKey)={
    val signedMsg = createSignMsg(tx, meta)
    val jsonString = toCanonicalJSONBytes(signedMsg)

    println("PrivateKeyAsHex---"+key.getPrivateKeyAsHex)

    println("PriavetKeyasBigInt--"+key.getPrivKey)
    println("PriavetKeyasBigInt22--"+new BigInteger(key.getPrivateKeyAsHex, 16))
    val privKey = key.getPrivKey
    val pubKey = new BigInteger(key.getPublicKeyAsHex, 16)

    val pubKey2 = Sign.publicKeyFromPrivate(privKey)
    println("PubKeyasBigInt--"+new BigInteger(key.getPublicKeyAsHex, 16))
    println("PubKeyasBigInt22--"+ Sign.publicKeyFromPrivate(privKey))
    val keyPair = new ECKeyPair(privKey,pubKey2)

    val msgHash = Web3jHash.sha3(jsonString.getBytes(StandardCharsets.UTF_8))
    println("hash ---"+msgHash.toList.toString())

    val signature = Sign.signMessage(msgHash,keyPair,false)
    println("R value --"+signature.getR.mkString(""))
    println("S value --"+signature.getS.mkString(""))
   println("Signature R--"+Hex.toHexString(signature.getR))
    println("Signature V--"+Hex.toHexString(signature.getV))
   // Sign.SignatureData.
  val completeByte = signature.getV ++ signature.getR ++ signature.getS
  val finalSig= new String(BouncyCastleBAse64.encode(completeByte), StandardCharsets.UTF_8)

    println(finalSig)
    println(Base64.getEncoder.encodeToString(completeByte))
    println(Base64.getUrlEncoder.encodeToString(completeByte))
  }

}