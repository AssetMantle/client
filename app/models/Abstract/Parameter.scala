package models.Abstract

import models.common.Parameters._
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, Reads, Writes}

abstract class Parameter {
  val parameterType: String

  def asAuthParameter: AuthParameter = this.asInstanceOf[AuthParameter]

  def asBankParameter: BankParameter = this.asInstanceOf[BankParameter]

  def asCrisisParameter: CrisisParameter = this.asInstanceOf[CrisisParameter]

  def asDistributionParameter: DistributionParameter = this.asInstanceOf[DistributionParameter]

  def asGovernanceParameter: GovernanceParameter = this.asInstanceOf[GovernanceParameter]

  def asIBCParameter: IBCParameter = this.asInstanceOf[IBCParameter]

  def asMintingParameter: MintingParameter = this.asInstanceOf[MintingParameter]

  def asSlashingParameter: SlashingParameter = this.asInstanceOf[SlashingParameter]

  def asStakingParameter: StakingParameter = this.asInstanceOf[StakingParameter]

  def asClassificationParameter: ClassificationParameter = this.asInstanceOf[ClassificationParameter]

  def asIdentityParameter: IdentityParameter = this.asInstanceOf[IdentityParameter]

  def asOrderParameter: OrderParameter = this.asInstanceOf[OrderParameter]

  def asSplitParameter: SplitParameter = this.asInstanceOf[SplitParameter]

  def asAssetParameter: AssetParameter = this.asInstanceOf[AssetParameter]

  def asMetaParameter: MetaParameter = this.asInstanceOf[MetaParameter]

  def asMaintainerParameter: MaintainerParameter = this.asInstanceOf[MaintainerParameter]

  def asTransferParameter: TransferParameter = this.asInstanceOf[TransferParameter]

}

object Parameter {
  implicit val parameterWrites: Writes[Parameter] = {
    case authParameter: AuthParameter => Json.toJson(authParameter)
    case bankParameter: BankParameter => Json.toJson(bankParameter)
    case crisisParameter: CrisisParameter => Json.toJson(crisisParameter)
    case distributionParameter: DistributionParameter => Json.toJson(distributionParameter)
    case governanceParameter: GovernanceParameter => Json.toJson(governanceParameter)
    case ibcParameter: IBCParameter => Json.toJson(ibcParameter)
    case mintingParameter: MintingParameter => Json.toJson(mintingParameter)
    case slashingParameter: SlashingParameter => Json.toJson(slashingParameter)
    case stakingParameter: StakingParameter => Json.toJson(stakingParameter)
    case transferParameter: TransferParameter => Json.toJson(transferParameter)
    case assetParameter: AssetParameter => Json.toJson(assetParameter)
    case classificationParameter: ClassificationParameter => Json.toJson(classificationParameter)
    case identityParameter: IdentityParameter => Json.toJson(identityParameter)
    case maintainerParameter: MaintainerParameter => Json.toJson(maintainerParameter)
    case metaParameter: MetaParameter => Json.toJson(metaParameter)
    case orderParameter: OrderParameter => Json.toJson(orderParameter)
    case splitParameter: SplitParameter => Json.toJson(splitParameter)
  }

  implicit val parameterReads: Reads[Parameter] = {
    Json.format[AuthParameter].map(x => x: Parameter) or
      Json.format[BankParameter].map(x => x: Parameter) or
      Json.format[CrisisParameter].map(x => x: Parameter) or
      Json.format[DistributionParameter].map(x => x: Parameter) or
      Json.format[GovernanceParameter].map(x => x: Parameter) or
      Json.format[IBCParameter].map(x => x: Parameter) or
      Json.format[MintingParameter].map(x => x: Parameter) or
      Json.format[SlashingParameter].map(x => x: Parameter) or
      Json.format[StakingParameter].map(x => x: Parameter) or
      Json.format[TransferParameter].map(x => x: Parameter) or
      Json.format[AssetParameter].map(x => x: Parameter) or
      Json.format[ClassificationParameter].map(x => x: Parameter) or
      Json.format[IdentityParameter].map(x => x: Parameter) or
      Json.format[MaintainerParameter].map(x => x: Parameter) or
      Json.format[MetaParameter].map(x => x: Parameter) or
      Json.format[SplitParameter].map(x => x: Parameter) or
      Json.format[OrderParameter].map(x => x: Parameter)
  }
}
