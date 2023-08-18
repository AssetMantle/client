package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import schema.id.{base => baseSchemaID}

object ID {

  case class HashID(i_d_bytes: Option[String]) {
    def toHashID: baseSchemaID.HashID = if (this.i_d_bytes.isDefined) baseSchemaID.HashID(utilities.Secrets.base64Decoder(this.i_d_bytes.get))
    else baseSchemaID.HashID(Array[Byte]())
  }

  implicit val HashIDReads: Reads[HashID] = Json.reads[HashID]

  case class StringID(i_d_string: String) {
    def toStringID: baseSchemaID.StringID = baseSchemaID.StringID(this.i_d_string)
  }

  implicit val StringIDReads: Reads[StringID] = Json.reads[StringID]

  case class AssetID(hash_i_d: HashID) {
    def toAssetID: baseSchemaID.AssetID = baseSchemaID.AssetID(this.hash_i_d.toHashID)
  }

  implicit val AssetIDReads: Reads[AssetID] = Json.reads[AssetID]

  case class ClassificationID(hash_i_d: HashID) {
    def toClassificationID: baseSchemaID.ClassificationID = baseSchemaID.ClassificationID(this.hash_i_d.toHashID)
  }

  implicit val ClassificationIDReads: Reads[ClassificationID] = Json.reads[ClassificationID]

  case class DataID(type_i_d: StringID, hash_i_d: HashID) {
    def toDataID: baseSchemaID.DataID = baseSchemaID.DataID(typeID = this.type_i_d.toStringID, hashID = this.hash_i_d.toHashID)
  }

  implicit val DataIDReads: Reads[DataID] = Json.reads[DataID]

  case class IdentityID(hash_i_d: HashID) {
    def toIdentityID: baseSchemaID.IdentityID = baseSchemaID.IdentityID(this.hash_i_d.toHashID)
  }

  implicit val IdentityIDReads: Reads[IdentityID] = Json.reads[IdentityID]

  case class MaintainerID(hash_i_d: HashID) {
    def toMaintainerID: baseSchemaID.MaintainerID = baseSchemaID.MaintainerID(this.hash_i_d.toHashID)
  }

  implicit val MaintainerIDReads: Reads[MaintainerID] = Json.reads[MaintainerID]

  case class OrderID(hash_i_d: HashID) {
    def toOrderID: baseSchemaID.OrderID = baseSchemaID.OrderID(this.hash_i_d.toHashID)
  }

  implicit val OrderIDReads: Reads[OrderID] = Json.reads[OrderID]

  case class PropertyID(key_i_d: StringID, type_i_d: StringID) {
    def toPropertyID: baseSchemaID.PropertyID = baseSchemaID.PropertyID(keyID = this.key_i_d.toStringID, typeID = this.type_i_d.toStringID)
  }

  implicit val PropertyIDReads: Reads[PropertyID] = Json.reads[PropertyID]

  case class SplitID(asset_i_d: AssetID, owner_i_d: IdentityID) {
    def toSplitID: baseSchemaID.SplitID = baseSchemaID.SplitID(ownerID = this.owner_i_d.toIdentityID, assetID = this.asset_i_d.toAssetID)
  }

  implicit val SplitIDReads: Reads[SplitID] = Json.reads[SplitID]

  case class AnyID(
                    asset_i_d: Option[AssetID],
                    classification_i_d: Option[ClassificationID],
                    data_i_d: Option[DataID],
                    hash_i_d: Option[HashID],
                    identity_i_d: Option[IdentityID],
                    maintainer_i_d: Option[MaintainerID],
                    order_i_d: Option[OrderID],
                    property_i_d: Option[PropertyID],
                    split_i_d: Option[SplitID],
                    string_i_d: Option[StringID]) {

    def toID: schema.id.ID =
      if (this.asset_i_d.isDefined) this.asset_i_d.get.toAssetID
      else if (this.classification_i_d.isDefined) this.classification_i_d.get.toClassificationID
      else if (this.data_i_d.isDefined) this.data_i_d.get.toDataID
      else if (this.hash_i_d.isDefined) this.hash_i_d.get.toHashID
      else if (this.identity_i_d.isDefined) this.identity_i_d.get.toIdentityID
      else if (this.maintainer_i_d.isDefined) this.maintainer_i_d.get.toMaintainerID
      else if (this.order_i_d.isDefined) this.order_i_d.get.toOrderID
      else if (this.property_i_d.isDefined) this.property_i_d.get.toPropertyID
      else if (this.split_i_d.isDefined) this.split_i_d.get.toSplitID
      else this.string_i_d.get.toStringID
  }

  implicit val AnyIDReads: Reads[AnyID] = Json.reads[AnyID]

}
