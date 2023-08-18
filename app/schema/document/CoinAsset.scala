package schema.document

import schema.data.base.StringData
import schema.id.base.{AssetID, ClassificationID}
import schema.qualified.{Immutables, Mutables}

object CoinAsset {

  val DocumentImmutables: Immutables = Immutables(Seq(schema.constants.Properties.DenomProperty))
  val DocumentMutables: Mutables = Mutables(Seq())
  val DocumentClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(immutables = DocumentImmutables, mutables = DocumentMutables)

  def getCoinAssetImmutables(denom: String): Immutables = Immutables(Seq(schema.constants.Properties.DenomProperty.mutate(StringData(denom))))

  def getCoinAssetDocument(denom: String): Document = Document(classificationID = DocumentClassificationID, immutables = getCoinAssetImmutables(denom), mutables = DocumentMutables)

  def getCoinAssetID(denom: String): AssetID = schema.utilities.ID.getAssetID(classificationID = DocumentClassificationID, immutables = getCoinAssetImmutables(denom))

}
