package schema.document

import schema.constants.Properties._
import schema.data.base.{HeightData, IDData, NumberData}
import schema.id.base._
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}

object PutOrder {
  private val PutOrderImmutables: Immutables = Immutables(PropertyList(Seq(
    MakerIDProperty,
    MakerAssetIDProperty,
    TakerAssetIDProperty,
    MakerSplitProperty,
    TakerSplitProperty,
    ExpiryHeightProperty)))
  private val PutOrderMutables: Mutables = Mutables(Seq())

  val PutOrderClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(PutOrderImmutables, PutOrderMutables)

  def getPutOrderImmutables(makerID: IdentityID, makerAssetID: AssetID, takerAssetID: AssetID, makerSplit: NumberData, takerSplit: NumberData, expiryHeight: HeightData): Immutables = Immutables(PropertyList(Seq(
    MakerIDProperty.mutate(IDData(makerID)),
    MakerAssetIDProperty.mutate(IDData(makerAssetID)),
    TakerAssetIDProperty.mutate(IDData(takerAssetID)),
    MakerSplitProperty.mutate(makerSplit),
    TakerSplitProperty.mutate(takerSplit),
    ExpiryHeightProperty.mutate(expiryHeight),
  )))

  def getPutOrderDocument(makerID: IdentityID, makerAssetID: AssetID, takerAssetID: AssetID, makerSplit: NumberData, takerSplit: NumberData, expiryHeight: HeightData): Document = Document(classificationID = PutOrderClassificationID,
    immutables = getPutOrderImmutables(makerID = makerID, makerAssetID = makerAssetID, takerAssetID = takerAssetID, makerSplit = makerSplit, takerSplit = takerSplit, expiryHeight = expiryHeight),
    mutables = PutOrderMutables)

  def getPutOrderID(makerID: IdentityID, makerAssetID: AssetID, takerAssetID: AssetID, makerSplit: NumberData, takerSplit: NumberData, expiryHeight: HeightData): OrderID = schema.utilities.ID.getOrderID(classificationID = PutOrderClassificationID, immutables = getPutOrderImmutables(makerID = makerID, makerAssetID = makerAssetID, takerAssetID = takerAssetID, makerSplit = makerSplit, takerSplit = takerSplit, expiryHeight = expiryHeight))

}
