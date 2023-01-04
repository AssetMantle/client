package queries.responses.common

import play.api.libs.json.{Json, OFormat, Reads}
import queries.Abstract.PublicKey

object PublicKeys {

  case class SinglePublicKey(`@type`: String, key: String) extends PublicKey {

    def getType: String = this.`@type`

    def getBytes: Array[Byte] = utilities.Secrets.base64Decoder(this.key)

  }

  implicit val singlePublicKeyReads: Reads[SinglePublicKey] = Json.reads[SinglePublicKey]

  implicit val singleSigPublicKeyFormat: OFormat[SinglePublicKey] = Json.format[SinglePublicKey]

  case class MultiSigPublicKey(`@type`: String, threshold: Int, public_keys: Seq[SinglePublicKey]) extends PublicKey {
    def getType: String = this.`@type`

    def getBytes: Array[Byte] = Array()
  }

  implicit val multiSigPublicKeyReads: Reads[MultiSigPublicKey] = Json.reads[MultiSigPublicKey]

  implicit val multiSigPublicKeyFormat: OFormat[MultiSigPublicKey] = Json.format[MultiSigPublicKey]

}
