package queries.responses.common

import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.Abstract.PublicKey
import models.common.{PublicKeys => SeriliazablePublicKeys}
import play.api.libs.functional.syntax._
import models.Abstract.{PublicKey => SerializablePublicKey}
import play.api.Logger

object PublicKey {

  private implicit val module: String = constants.Module.PUBLIC_KEY_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class SinglePublicKey(publicKeyType: String, key: String) extends PublicKey {
    def toSerializablePublicKey: SerializablePublicKey = SeriliazablePublicKeys.SinglePublicKey(publicKeyType = publicKeyType, key = key)
  }

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = (
    (JsPath \ "@type").read[String] and
      (JsPath \ "key").read[String]
    ) (SinglePublicKey.apply _)

  case class MultiSigPublicKey(publicKeyType: String, threshold: Int, public_keys: Seq[SinglePublicKey]) extends PublicKey {
    def toSerializablePublicKey: SerializablePublicKey = SeriliazablePublicKeys.MultiSigPublicKey(
      publicKeyType = publicKeyType,
      threshold = threshold,
      publicKeys = public_keys.map(x => SeriliazablePublicKeys.SinglePublicKey(publicKeyType = x.publicKeyType, key = x.key))
    )
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = (
    (JsPath \ "@type").read[String] and
      (JsPath \ "threshold").read[Int] and
      (JsPath \ "public_keys").read[Seq[SinglePublicKey]]
    ) (MultiSigPublicKey.apply _)

  def publicKeyApply(publicKeyType: String, value: JsObject): PublicKey = publicKeyType match {
    case constants.Blockchain.PublicKey.SINGLE => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
    case constants.Blockchain.PublicKey.MULTI_SIG => utilities.JSON.convertJsonStringToObject[MultiSigPublicKey](value.toString)
    case constants.Blockchain.PublicKey.VALIDATOR => utilities.JSON.convertJsonStringToObject[SinglePublicKey](value.toString)
  }

  implicit val publicKeyReads: Reads[PublicKey] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (publicKeyApply _)
}
