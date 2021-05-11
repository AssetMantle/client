package queries.Abstract

import models.Abstract.{PublicKey => SerializablePublicKey}

abstract class PublicKey {
  def toSerializablePublicKey: SerializablePublicKey
}
