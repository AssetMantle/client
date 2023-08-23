package queries.responses.blockchain.common

import models.blockchain._
import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._
import schema.data.base.IDData
import schema.property.base.MetaProperty

case class Document(classification_i_d: ClassificationID, immutables: Immutables, mutables: Mutables) {

  def toAsset: Asset = {
    val assetID = schema.utilities.ID.getAssetID(classificationID = this.classification_i_d.toClassificationID, immutables = this.immutables.toImmutables)
    Asset(id = assetID.getBytes, idString = assetID.asString, classificationID = this.classification_i_d.toClassificationID.getBytes, immutables = this.immutables.toImmutables.getProtoBytes, mutables = this.mutables.toMutables.getProtoBytes)
  }

  def toClassification: Classification = {
    Classification(this.classification_i_d.toClassificationID.getBytes, idString = this.classification_i_d.toClassificationID.asString, immutables = this.immutables.toImmutables.asProtoImmutables.toByteString.toByteArray, mutables = this.mutables.toMutables.asProtoMutables.toByteString.toByteArray, classificationType = "")
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
    Maintainer(
      id = maintainerID.getBytes,
      idString = maintainerID.asString,
      maintainedClassificationID = schema.id.base.ClassificationID(
        IDData(
          MetaProperty(this.immutables.toImmutables.propertyList
            .getProperty(schema.constants.Properties.MaintainedClassificationIDProperty.getID)
            .getOrElse(throw new IllegalArgumentException("MAINTAINED_CLASSIFICATION_ID_NOT_FOUND")).getProtoBytes)
            .getData.getProtoBytes)
          .getAnyID.getClassificationID)
        .getBytes,
      immutables = this.immutables.toImmutables.getProtoBytes,
      mutables = this.mutables.toMutables.getProtoBytes)
  }

}

object Document {
  implicit val DocumentReads: Reads[Document] = Json.reads[Document]
}