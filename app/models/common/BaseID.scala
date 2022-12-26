package models.common

import models.Abstract.AnyIDImpl
import play.api.libs.json.{Json, OWrites, Reads}

object BaseID {

  case class HashID(idBytes: Array[Byte]) extends AnyIDImpl

  object HashID {
    implicit val hashIDReads: Reads[HashID] = Json.reads[HashID]

    implicit val hashIDWrites: OWrites[HashID] = Json.writes[HashID]
  }

  case class AssetID(hashID: HashID) extends AnyIDImpl

  object AssetID {
    implicit val assetIDReads: Reads[AssetID] = Json.reads[AssetID]

    implicit val assetIDWrites: OWrites[AssetID] = Json.writes[AssetID]
  }

  case class ClassificationID(hashID: HashID) extends AnyIDImpl

  object ClassificationID {
    implicit val classificationIDReads: Reads[ClassificationID] = Json.reads[ClassificationID]

    implicit val classificationIDWrites: OWrites[ClassificationID] = Json.writes[ClassificationID]
  }

  case class DataID(typeID: String, hashID: String) extends AnyIDImpl

  object DataID {
    implicit val dataIDReads: Reads[DataID] = Json.reads[DataID]

    implicit val dataIDWrites: OWrites[DataID] = Json.writes[DataID]
  }

  case class IdentityID(hashID: HashID) extends AnyIDImpl

  object IdentityID {
    implicit val identityIDReads: Reads[IdentityID] = Json.reads[IdentityID]

    implicit val identityIDWrites: OWrites[IdentityID] = Json.writes[IdentityID]
  }

  case class MaintainerID(hashID: HashID) extends AnyIDImpl

  object MaintainerID {
    implicit val maintainerIDReads: Reads[MaintainerID] = Json.reads[MaintainerID]

    implicit val maintainerIDWrites: OWrites[MaintainerID] = Json.writes[MaintainerID]
  }

  case class OrderID(hashID: HashID) extends AnyIDImpl

  object OrderID {
    implicit val orderIDReads: Reads[OrderID] = Json.reads[OrderID]

    implicit val orderIDWrites: OWrites[OrderID] = Json.writes[OrderID]
  }


  case class StringID(idString: String) extends AnyIDImpl

  object StringID {
    implicit val reads: Reads[StringID] = Json.reads[StringID]

    implicit val writes: OWrites[StringID] = Json.writes[StringID]
  }

  case class OwnableID(stringID: StringID) extends AnyIDImpl

  object OwnableID {
    implicit val reads: Reads[OwnableID] = Json.reads[OwnableID]

    implicit val writes: OWrites[OwnableID] = Json.writes[OwnableID]
  }

  case class PropertyID(keyID: String, typeID: String) extends AnyIDImpl

  object PropertyID {
    implicit val reads: Reads[PropertyID] = Json.reads[PropertyID]

    implicit val writes: OWrites[PropertyID] = Json.writes[PropertyID]
  }

  case class SplitID(identityID: IdentityID, ownableID: OwnableID) extends AnyIDImpl

  object SplitID {
    implicit val reads: Reads[SplitID] = Json.reads[SplitID]

    implicit val writes: OWrites[SplitID] = Json.writes[SplitID]
  }
}
