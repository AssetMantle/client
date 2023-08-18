package queries.responses.blockchain.common

import models.common.Parameters.{AssetParameter, ClassificationParameter, IdentityParameter, MaintainerParameter, MetaParameter, OrderParameter, SplitParameter}
import play.api.libs.json.{Json, Reads}
import schema.data.{base => baseSchemaData}
import schema.property.base.MetaProperty

case class ParameterList(parameters: Seq[Parameter]) {
  def getMetaProperty(propertyName: String): MetaProperty = this.parameters.find(_.meta_property.i_d.key_i_d.i_d_string == propertyName).fold(throw new IllegalArgumentException("property not found"))(_.meta_property.toMetaProperty)

  def getAssetParameter: AssetParameter = AssetParameter(
    burnEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.BurnEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
    mintEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.MintEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
    renumerateEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.RenumerateEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
    wrapAllowedCoins = baseSchemaData.ListData(this.getMetaProperty(schema.constants.Properties.WrapAllowedCoinsProperty.getID.keyID.value).getData.getProtoBytes).getListableDataList.map(x => baseSchemaData.IDData(x.getProtoBytes).getID.asString),
    unwrapAllowedCoins = baseSchemaData.ListData(this.getMetaProperty(schema.constants.Properties.UnwrapAllowedCoinsProperty.getID.keyID.value).getData.getProtoBytes).getListableDataList.map(x => baseSchemaData.IDData(x.getProtoBytes).getID.asString))

  def getClassificationParameter: ClassificationParameter = ClassificationParameter(
    bondRate = baseSchemaData.NumberData(this.getMetaProperty(schema.constants.Properties.BondRateProperty.getID.keyID.value).getData.getProtoBytes).value,
    maxPropertyCount = baseSchemaData.NumberData(this.getMetaProperty(schema.constants.Properties.MaxPropertyCountProperty.getID.keyID.value).getData.getProtoBytes).value,
    defineEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.DefineEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
  )

  def getIdentityParameter: IdentityParameter = IdentityParameter(
    issueEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.IssueEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
    quashEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.QuashEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
    maxProvisionAddressCount = baseSchemaData.NumberData(this.getMetaProperty(schema.constants.Properties.MaxProvisionAddressCountProperty.getID.keyID.value).getData.getProtoBytes).value)

  def getMaintainerParameter: MaintainerParameter = MaintainerParameter(
    deputizeAllowed = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.DeputizeAllowedProperty.getID.keyID.value).getData.getProtoBytes).value)

  def getMetaParameter: MetaParameter = MetaParameter(
    revealEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.RevealEnabledProperty.getID.keyID.value).getData.getProtoBytes).value)

  def getOrderParameter: OrderParameter = OrderParameter(
    maxOrderLife = baseSchemaData.HeightData(this.getMetaProperty(schema.constants.Properties.MaxOrderLifeProperty.getID.keyID.value).getData.getProtoBytes).value.value,
    putEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.PutEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
  )

  def getSplitParameter: SplitParameter = SplitParameter(
    transferEnabled = baseSchemaData.BooleanData(this.getMetaProperty(schema.constants.Properties.TransferEnabledProperty.getID.keyID.value).getData.getProtoBytes).value,
  )

}

object ParameterList {
  implicit val ParameterListReads: Reads[ParameterList] = Json.reads[ParameterList]
}
