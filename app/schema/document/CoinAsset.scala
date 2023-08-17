package schema.document

import schema.data.base.StringData
import schema.id.base.{AssetID, ClassificationID}
import schema.qualified.{Immutables, Mutables}

object CoinAsset {

  val CoinAssetClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(immutables = Immutables(Seq(schema.constants.Properties.DenomProperty)), mutables = Mutables(Seq()))

  def getCoinAssetImmutables(denom: String): Immutables = Immutables(Seq(schema.constants.Properties.DenomProperty.mutate(StringData(denom))))

  def getCoinAssetDocument(denom: String): Document = Document(classificationID = CoinAssetClassificationID, immutables = getCoinAssetImmutables(denom), mutables = Mutables(Seq()))

  def getCoinAssetID(denom: String): AssetID = schema.utilities.ID.getAssetID(classificationID = CoinAssetClassificationID, immutables = getCoinAssetImmutables(denom))

}
