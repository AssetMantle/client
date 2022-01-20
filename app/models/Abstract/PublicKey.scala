package models.Abstract

import exceptions.BaseException
import models.common.PublicKeys._
import play.api.Logger
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json._

abstract class PublicKey {
  val publicKeyType: String
  val value: String

  def isValidatorKey: Boolean = publicKeyType == constants.Blockchain.PublicKey.VALIDATOR

  def getAccountAddress: String = utilities.Bech32.convertAccountPublicKeyToAccountAddress(value)
}

object PublicKey {
  private implicit val module: String = constants.Module.PUBLIC_KEYS

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val publicKeyWrites: Writes[PublicKey] = {
    case singlePublicKey: SinglePublicKey => Json.toJson(singlePublicKey)
    case multiSigPublicKey: MultiSigPublicKey => Json.toJson(multiSigPublicKey)
    case _ => throw new BaseException(constants.Response.NO_SUCH_PUBLIC_KEY_TYPE)
  }
  implicit val publicKeyReads: Reads[PublicKey] = {
    Json.format[SinglePublicKey].map(x => x: SinglePublicKey) or
      Json.format[MultiSigPublicKey].map(x => x: MultiSigPublicKey)
  }
}