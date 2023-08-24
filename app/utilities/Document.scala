package utilities

import models.blockchain._
import schema.document.Document

object Document {

  def getIdentity(document: Document): Identity = {
    val identityID = schema.utilities.ID.getIdentityID(document.classificationID, document.immutables)
    Identity(
      id = identityID.getBytes,
      idString = identityID.asString,
      classificationID = document.classificationID.getBytes,
      immutables = document.immutables.getProtoBytes,
      mutables = document.mutables.getProtoBytes)
  }

  def getClassification(document: Document, classificationType: String): Classification = {
    val classificationID = schema.utilities.ID.getClassificationID(document.immutables, document.mutables)
    Classification(
      id = classificationID.getBytes,
      idString = classificationID.asString,
      immutables = document.immutables.getProtoBytes,
      mutables = document.mutables.getProtoBytes,
      classificationType = classificationType
    )
  }

  def getAsset(document: Document): Asset = {
    val assetID = schema.utilities.ID.getAssetID(document.classificationID, document.immutables)
    Asset(
      id = assetID.getBytes,
      idString = assetID.asString,
      classificationID = document.classificationID.getBytes,
      immutables = document.immutables.getProtoBytes,
      mutables = document.mutables.getProtoBytes)
  }

  def getOrder(document: Document): Order = {
    val orderID = schema.utilities.ID.getOrderID(document.classificationID, document.immutables)
    Order(
      id = orderID.getBytes,
      idString = orderID.asString,
      classificationID = document.classificationID.getBytes,
      immutables = document.immutables.getProtoBytes,
      mutables = document.mutables.getProtoBytes)
  }

//  def getMaintainer(document: Document): Maintainer = {
//    val maintainerID = schema.utilities.ID.getMaintainerID(document.classificationID, document.immutables)
//    Maintainer(
//      id = maintainerID.getBytes,
//      idString = maintainerID.asString,
//      maintainedClassificationID = ,
//      immutables = document.immutables.getProtoBytes,
//      mutables = document.mutables.getProtoBytes)
//  }

}
