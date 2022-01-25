package models.common

import models.Abstract.PublicKey
import play.api.Logger
import play.api.libs.json.{Json, OWrites, Reads, Writes}

object PublicKeys {

  private implicit val module: String = constants.Module.PUBLIC_KEYS

  private implicit val logger: Logger = Logger(this.getClass)

  case class SinglePublicKey(publicKeyType: String, key: String) extends PublicKey {
    val value: String = key
  }

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = Json.reads[SinglePublicKey]

  implicit val singlePublicKeyWrites: Writes[SinglePublicKey] = Json.writes[SinglePublicKey]

  case class MultiSigPublicKey(publicKeyType: String, threshold: Int, publicKeys: Seq[SinglePublicKey]) extends PublicKey {
    val value: String = publicKeys.map(_.key).mkString(", ")
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = Json.reads[MultiSigPublicKey]

  implicit val multiSigPublicKeyWrites: OWrites[MultiSigPublicKey] = Json.writes[MultiSigPublicKey]

  //  def publicKeyApply(publicKeyType: String, value: JsObject): PublicKey = publicKeyType match {
  //    case constants.Blockchain.PublicKey.SINGLE => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
  //    case constants.Blockchain.PublicKey.MULTI_SIG => utilities.JSON.convertJsonStringToObject[MultiSigPublicKey](value.toString)
  //    case constants.Blockchain.PublicKey.VALIDATOR => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
  //    case _ => throw new BaseException(constants.Response.NO_SUCH_PUBLIC_KEY_TYPE)
  //  }
}
