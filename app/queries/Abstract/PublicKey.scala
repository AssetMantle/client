package queries.Abstract

import models.Abstract.{PublicKey => SerializablePublicKey}
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads}
import queries.responses.common.PublicKeys.{MultiSigPublicKey, SinglePublicKey}

abstract class PublicKey {
  def toSerializablePublicKey: SerializablePublicKey
}

object PublicKey {
  implicit val publicKeyReads: Reads[PublicKey] = {
    Json.format[SinglePublicKey].map(x => x: SinglePublicKey) or
      Json.format[MultiSigPublicKey].map(x => x: MultiSigPublicKey)
  }
}
