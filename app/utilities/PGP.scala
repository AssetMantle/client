package utilities

import java.io._
import java.security.{NoSuchProviderException, Security, SignatureException}
import java.util.Iterator

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.operator.bc.{BcKeyFingerprintCalculator, BcPBESecretKeyDecryptorBuilder, BcPGPDigestCalculatorProvider, BcPublicKeyDataDecryptorFactory}
import org.bouncycastle.openpgp.operator.jcajce.{JcaKeyFingerprintCalculator, JcaPGPContentVerifierBuilderProvider}
import org.bouncycastle.openpgp._
import org.bouncycastle.util.io.Streams

object PGP {
  @throws[IOException]
  @throws[NoSuchProviderException]
  def decryptFile(inputFileName: String, defaultFileName: String, signerPublicKeyFileName: String, decryptorPrivateKeyLocation: String, decryptorPrivateKeyPassword: String): Unit = {
    val inputBytes = new BufferedInputStream(new FileInputStream(new File(inputFileName)))
    val keyFileBytes = new BufferedInputStream(new FileInputStream(new File(signerPublicKeyFileName)))
    val fOut = new FileOutputStream(new File(defaultFileName))
    decryptAndVerify(inputBytes, fOut, keyFileBytes, decryptorPrivateKeyLocation, decryptorPrivateKeyPassword)
    keyFileBytes.close
    inputBytes.close

  }

  @throws[IOException]
  @throws[SignatureException]
  @throws[PGPException]
  def decryptAndVerify(input: InputStream, fOut: FileOutputStream, publicKeyIn: InputStream, decryptorPrivateKeyLocation: String, decryptorPrivateKeyPassword: String): Unit = {

    Security.addProvider(new BouncyCastleProvider)
    val in = PGPUtil.getDecoderStream(input)
    val pgpF = new PGPObjectFactory(in, new BcKeyFingerprintCalculator)
    var enc: PGPEncryptedDataList = null
    val o = pgpF.nextObject
    //
    // the first object might be a PGP marker packet.
    if (o.isInstanceOf[PGPEncryptedDataList]) enc = o.asInstanceOf[PGPEncryptedDataList]
    else enc = pgpF.nextObject.asInstanceOf[PGPEncryptedDataList]
    // find the secret key
    val it: Iterator[_] = enc.getEncryptedDataObjects
    var sKey: PGPPrivateKey = null
    var pbe: PGPPublicKeyEncryptedData = null


    val privateKeyStream = new BufferedInputStream(new FileInputStream(new File(decryptorPrivateKeyLocation)))
    val pgpSec: PGPSecretKeyRingCollection = new PGPSecretKeyRingCollection(
      PGPUtil.getDecoderStream(privateKeyStream), new JcaKeyFingerprintCalculator())
    while ( {
      sKey == null && it.hasNext
    }) {
      pbe = it.next.asInstanceOf[PGPPublicKeyEncryptedData]
      val decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider).build(decryptorPrivateKeyPassword.toCharArray)
      //      val psKey: PGPSecretKey = INSTANCE._secretKeyRingCollection.getSecretKey(pbe.getKeyID)
      val psKey: PGPSecretKey = pgpSec.getSecretKey(pbe.getKeyID)
      if (psKey != null) sKey = psKey.extractPrivateKey(decryptor)
    }
    if (sKey == null) throw new IllegalArgumentException("Unable to find secret key to decrypt the message")
    val clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey))
    var plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator())
    var message: Object = null
    var onePassSignatureList: PGPOnePassSignatureList = null
    var signatureList: PGPSignatureList = null
    var compressedData: PGPCompressedData = null
    message = plainFact.nextObject
    val actualOutput = new ByteArrayOutputStream
    while ( {
      message != null
    }) {
      if (message.isInstanceOf[PGPCompressedData]) {
        compressedData = message.asInstanceOf[PGPCompressedData]
        plainFact = new PGPObjectFactory(compressedData.getDataStream, new JcaKeyFingerprintCalculator)
        message = plainFact.nextObject
      }
      if (message.isInstanceOf[PGPLiteralData]) { // have to read it and keep it somewhere.
        Streams.pipeAll(message.asInstanceOf[PGPLiteralData].getInputStream, actualOutput)
      }
      else if (message.isInstanceOf[PGPOnePassSignatureList]) onePassSignatureList = message.asInstanceOf[PGPOnePassSignatureList]
      else if (message.isInstanceOf[PGPSignatureList]) signatureList = message.asInstanceOf[PGPSignatureList]
      else throw new PGPException("message unknown message type.")
      message = plainFact.nextObject
    }
    actualOutput.close()
    var publicKey: PGPPublicKey = null
    val output = actualOutput.toByteArray
    if (onePassSignatureList == null || signatureList == null) throw new PGPException("Poor PGP. Signatures not found.")
    else for (i <- 0 until onePassSignatureList.size) {
      val ops = onePassSignatureList.get(0)
      val pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn), new JcaKeyFingerprintCalculator)
      publicKey = pgpRing.getPublicKey(ops.getKeyID)
      if (publicKey != null) {
        ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey)
        ops.update(output)
        val signature = signatureList.get(i)
        if (ops.verify(signature)) {
          val userIds = publicKey.getUserIDs
          while ( {
            userIds.hasNext
          }) {
            val userId = userIds.next.asInstanceOf[String]
          }
        }
        else throw new SignatureException("Signature verification failed")
      }
    }
    if (pbe.isIntegrityProtected && !pbe.verify) throw new PGPException("Data is integrity protected but integrity is lost.")
    else if (publicKey == null) throw new SignatureException("Signature not found")
    else {
      fOut.write(output)
      fOut.flush
      fOut.close
    }
    privateKeyStream.close()
  }
}
