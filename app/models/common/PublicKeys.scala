package models.common

import exceptions.BaseException
import models.Abstract.PublicKey
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsObject, JsPath, JsValue, Json, OWrites, Reads, Writes}

object PublicKeys {

  private implicit val module: String = constants.Module.PUBLIC_KEYS

  private implicit val logger: Logger = Logger(this.getClass)

  case class SinglePublicKey(publicKeyType: String, value: String) extends PublicKey

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = Json.reads[SinglePublicKey]

  implicit val singlePublicKeyWrites: OWrites[SinglePublicKey] = Json.writes[SinglePublicKey]

  case class MultiSigPublicKey(publicKeyType: String, threshold: Int, publicKeys: Seq[SinglePublicKey]) extends PublicKey {
    val value: String = publicKeys.map(_.value).mkString("")
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = Json.reads[MultiSigPublicKey]

  implicit val multiSigPublicKeyWrites: OWrites[MultiSigPublicKey] = Json.writes[MultiSigPublicKey]

  def publicKeyApply(publicKeyType: String, value: JsObject): PublicKey = publicKeyType match {
    case constants.Blockchain.PublicKey.SINGLE => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
    case constants.Blockchain.PublicKey.MULTI_SIG => utilities.JSON.convertJsonStringToObject[MultiSigPublicKey](value.toString)
    case constants.Blockchain.PublicKey.VALIDATOR => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
    case _ => throw new BaseException(constants.Response.NO_SUCH_PUBLIC_KEY_TYPE)
  }

  implicit val publicKeyReads: Reads[PublicKey] = (
    (JsPath \ "publicKeyType").read[String] and
      JsPath.read[JsObject]
    ) (publicKeyApply _)

  implicit val publicKeyWrites: Writes[PublicKey] = {
    case singlePublicKey: SinglePublicKey => Json.toJson(singlePublicKey)(Json.writes[SinglePublicKey])
    case multiSigPublicKey: MultiSigPublicKey => Json.toJson(multiSigPublicKey)(Json.writes[MultiSigPublicKey])
    case _ => throw new BaseException(constants.Response.NO_SUCH_PUBLIC_KEY_TYPE)
  }
}
