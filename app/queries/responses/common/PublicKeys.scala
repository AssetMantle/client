package queries.responses.common

import play.api.libs.json.{JsObject, JsPath, Json, OFormat, Reads}
import queries.Abstract.PublicKey
import models.common.{PublicKeys => SeriliazablePublicKeys}
import play.api.libs.functional.syntax._
import models.Abstract.{PublicKey => SerializablePublicKey}
import play.api.Logger

object PublicKeys {

  case class SinglePublicKey(`type`: String, value: String) extends PublicKey {
    def toSerializablePublicKey: SerializablePublicKey = SeriliazablePublicKeys.SinglePublicKey(publicKeyType = `type`, value = value)
  }

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = Json.reads[SinglePublicKey]

  implicit val singleSigPublicKeyFormat: OFormat[SinglePublicKey] = Json.format[SinglePublicKey]

  case class MultiSigPublicKey(`type`: String, threshold: Int, public_keys: Seq[SinglePublicKey]) extends PublicKey {
    def toSerializablePublicKey: SerializablePublicKey = SeriliazablePublicKeys.MultiSigPublicKey(
      publicKeyType = `type`,
      threshold = threshold,
      publicKeys = public_keys.map(x => SeriliazablePublicKeys.SinglePublicKey(publicKeyType = x.`type`, value = x.value))
    )
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = Json.reads[MultiSigPublicKey]

  implicit val multiSigPublicKeyFormat: OFormat[MultiSigPublicKey] = Json.format[MultiSigPublicKey]

  implicit val publicKeyReads: Reads[PublicKey] = {
    Json.format[SinglePublicKey].map(x => x: SinglePublicKey) or
      Json.format[MultiSigPublicKey].map(x => x: MultiSigPublicKey)
  }

}
