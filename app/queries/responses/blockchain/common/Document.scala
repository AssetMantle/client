package queries.responses.blockchain.common

import models.blockchain.{Asset, Classification, Identity, Order, Maintainer}
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._

case class Document(classification_i_d: ClassificationID, immutables: Immutables, mutables: Mutables) {

  def toAsset: Asset = {
    val assetID = schema.utilities.ID.getAssetID(classificationID = this.classification_i_d.toClassificationID, immutables = this.immutables.toImmutables)
    Asset(id = assetID.getBytes, idString = assetID.asString, classificationID = this.classification_i_d.toClassificationID.getBytes, immutables = this.immutables.toImmutables.getProtoBytes, mutables = this.mutables.toMutables.getProtoBytes)
  }

  def toClassification: Classification = {
    Classification(this.classification_i_d.toClassificationID.getBytes, idString = this.classification_i_d.toClassificationID.asString, immutables = this.immutables.toImmutables.asProtoImmutables.toByteString.toByteArray, mutables = this.mutables.toMutables.asProtoMutables.toByteString.toByteArray)
  }

  def toIdentity: Identity = {
    val identityID = schema.utilities.ID.getIdentityID(classificationID = this.classification_i_d.toClassificationID, immutables = this.immutables.toImmutables)
    Identity(id = identityID.getBytes, idString = identityID.asString, classificationID = this.classification_i_d.toClassificationID.getBytes, immutables = this.immutables.toImmutables.getProtoBytes, mutables = this.mutables.toMutables.getProtoBytes)
  }

  def toOrder: Order = {
    val orderID = schema.utilities.ID.getOrderID(classificationID = this.classification_i_d.toClassificationID, immutables = this.immutables.toImmutables)
    Order(id = orderID.getBytes, idString = orderID.asString, classificationID = this.classification_i_d.toClassificationID.getBytes, immutables = this.immutables.toImmutables.getProtoBytes, mutables = this.mutables.toMutables.getProtoBytes)
  }


  def toMaintainer: Maintainer = {
    val maintainerID = schema.utilities.ID.getMaintainerID(classificationID = this.classification_i_d.toClassificationID, immutables = this.immutables.toImmutables)
    Maintainer(id = maintainerID.getBytes, idString = maintainerID.asString, classificationID = this.classification_i_d.toClassificationID.getBytes, immutables = this.immutables.toImmutables.getProtoBytes, mutables = this.mutables.toMutables.getProtoBytes)
  }

}

object Document {
  implicit val DocumentReads: Reads[Document] = Json.reads[Document]
}