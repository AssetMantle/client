package queries.responses.blockchain.common

import models.common.Parameters.{AssetParameter, ClassificationParameter, IdentityParameter, MaintainerParameter, MetaParameter, OrderParameter, SplitParameter}
import play.api.libs.json.{Json, Reads}
import schema.data.{base => baseSchemaData}
import schema.property.base.MetaProperty

case class ParameterList(parameters: Seq[Parameter]) {
  def getMetaProperty(propertyName: String): MetaProperty = this.parameters.find(_.meta_property.i_d.key_i_d.i_d_string == propertyName).fold(throw new IllegalArgumentException("property not found"))(_.meta_property.toMetaProperty)

  def getAssetParameter: AssetParameter = AssetParameter(
    burnEnabled = baseSchemaData.BooleanData(this.getMetaProperty("burnEnabled").getData.getProtoBytes).value,
    mintEnabled = baseSchemaData.BooleanData(this.getMetaProperty("mintEnabled").getData.getProtoBytes).value,
    renumerateEnabled = baseSchemaData.BooleanData(this.getMetaProperty("renumerateEnabled").getData.getProtoBytes).value)

  def getClassificationParameter: ClassificationParameter = ClassificationParameter(
    bondRate = baseSchemaData.NumberData(this.getMetaProperty("bondRate").getData.getProtoBytes).value,
    maxPropertyCount = baseSchemaData.NumberData(this.getMetaProperty("maxPropertyCount").getData.getProtoBytes).value)

  def getIdentityParameter: IdentityParameter = IdentityParameter(
    maxProvisionAddressCount = baseSchemaData.NumberData(this.getMetaProperty("maxProvisionAddressCount").getData.getProtoBytes).value)

  def getMaintainerParameter: MaintainerParameter = MaintainerParameter(
    deputizeAllowed = baseSchemaData.BooleanData(this.getMetaProperty("deputizeAllowed").getData.getProtoBytes).value)

  def getMetaParameter: MetaParameter = MetaParameter(
    revealEnabled = baseSchemaData.BooleanData(this.getMetaProperty("revealEnabled").getData.getProtoBytes).value)

  def getOrderParameter: OrderParameter = OrderParameter(
    maxOrderLife = baseSchemaData.HeightData(this.getMetaProperty("maxOrderLife").getData.getProtoBytes).value.value)

  def getSplitParameter: SplitParameter = SplitParameter(
    wrapAllowedCoins = baseSchemaData.ListData(this.getMetaProperty("wrapAllowedCoins").getData.getProtoBytes).getListableDataList.map(x => baseSchemaData.IDData(x.getProtoBytes).getID.asString),
    unwrapAllowedCoins = baseSchemaData.ListData(this.getMetaProperty("unwrapAllowedCoins").getData.getProtoBytes).getListableDataList.map(x => baseSchemaData.IDData(x.getProtoBytes).getID.asString))
}

object ParameterList {
  implicit val ParameterListReads: Reads[ParameterList] = Json.reads[ParameterList]
}
