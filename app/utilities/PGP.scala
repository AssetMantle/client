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
  def decryptFile(inputFileName: String, fileOutputName: String, signerPublicKeyFileName: String, decryptorPrivateKeyLocation: String, decryptorPrivateKeyPassword: String): Unit = {
    val inputBytes = new BufferedInputStream(new FileInputStream(new File(inputFileName)))
    val publicKeyFileBites = new BufferedInputStream(new FileInputStream(new File(signerPublicKeyFileName)))
    val fileOutput = new FileOutputStream(new File(fileOutputName))
    decryptAndVerify(inputBytes, fileOutput, publicKeyFileBites, decryptorPrivateKeyLocation, decryptorPrivateKeyPassword)
    publicKeyFileBites.close
    inputBytes.close

  }

  @throws[IOException]
  @throws[SignatureException]
  @throws[PGPException]
  def decryptAndVerify(input: InputStream, fileOutput: FileOutputStream, publicKeyIn: InputStream, decryptorPrivateKeyLocation: String, decryptorPrivateKeyPassword: String): Unit = {

    Security.addProvider(new BouncyCastleProvider)
    val inputStream = PGPUtil.getDecoderStream(input)
    val pGPObjectFactory = new PGPObjectFactory(inputStream, new BcKeyFingerprintCalculator)
    var pGPEncryptedDataList: PGPEncryptedDataList = null
    val pgpFactoryNextObject = pGPObjectFactory.nextObject

    // the first object might be a PGP marker packet.
    if (pgpFactoryNextObject.isInstanceOf[PGPEncryptedDataList]) pGPEncryptedDataList = pgpFactoryNextObject.asInstanceOf[PGPEncryptedDataList]
    else pGPEncryptedDataList = pGPObjectFactory.nextObject.asInstanceOf[PGPEncryptedDataList]
    // find the secret key
    val iterator: Iterator[_] = pGPEncryptedDataList.getEncryptedDataObjects
    var pGPPrivateKey: PGPPrivateKey = null
    var pGPPublicKeyEncryptedData: PGPPublicKeyEncryptedData = null


    val privateKeyStream = new BufferedInputStream(new FileInputStream(new File(decryptorPrivateKeyLocation)))
    val pGPSecretKeyRingCollection: PGPSecretKeyRingCollection = new PGPSecretKeyRingCollection(
      PGPUtil.getDecoderStream(privateKeyStream), new JcaKeyFingerprintCalculator())
    while ( {
      pGPPrivateKey == null && iterator.hasNext
    }) {
      pGPPublicKeyEncryptedData = iterator.next.asInstanceOf[PGPPublicKeyEncryptedData]
      val decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider).build(decryptorPrivateKeyPassword.toCharArray)
      val pGPSecretKey: PGPSecretKey = pGPSecretKeyRingCollection.getSecretKey(pGPPublicKeyEncryptedData.getKeyID)
      if (pGPSecretKey != null) pGPPrivateKey = pGPSecretKey.extractPrivateKey(decryptor)
    }
    if (pGPPrivateKey == null) throw new IllegalArgumentException("Unable to find secret key to decrypt the message")
    val clear = pGPPublicKeyEncryptedData.getDataStream(new BcPublicKeyDataDecryptorFactory(pGPPrivateKey))
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
      if (message.isInstanceOf[PGPLiteralData]) {
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
      val pGPOnePassSignature = onePassSignatureList.get(0)
      val pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn), new JcaKeyFingerprintCalculator)
      publicKey = pgpRing.getPublicKey(pGPOnePassSignature.getKeyID)
      if (publicKey != null) {
        pGPOnePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey)
        pGPOnePassSignature.update(output)
        val signature = signatureList.get(i)
        if (pGPOnePassSignature.verify(signature)) {
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
    if (pGPPublicKeyEncryptedData.isIntegrityProtected && !pGPPublicKeyEncryptedData.verify) throw new PGPException("Data is integrity protected but integrity is lost.")
    else if (publicKey == null) throw new SignatureException("Signature not found")
    else {
      fileOutput.write(output)
      fileOutput.flush
      fileOutput.close
    }
    privateKeyStream.close()
  }
}
