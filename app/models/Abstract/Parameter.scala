package models.Abstract

import models.common.Parameters._

abstract class Parameter {
  val `type`: String

  def asAuthParameter: AuthParameter = this.asInstanceOf[AuthParameter]

  def asBankParameter: BankParameter = this.asInstanceOf[BankParameter]

  def asCrisisParameter: CrisisParameter = this.asInstanceOf[CrisisParameter]

  def asDistributionParameter: DistributionParameter = this.asInstanceOf[DistributionParameter]

  def asGovernanceParameter: GovernanceParameter = this.asInstanceOf[GovernanceParameter]

  def asHalvingParameter: HalvingParameter = this.asInstanceOf[HalvingParameter]

  def asIBCParameter: IBCParameter = this.asInstanceOf[IBCParameter]

  def asMintingParameter: MintingParameter = this.asInstanceOf[MintingParameter]

  def asSlashingParameter: SlashingParameter = this.asInstanceOf[SlashingParameter]

  def asStakingParameter: StakingParameter = this.asInstanceOf[StakingParameter]

  def asTransferParameter: TransferParameter = this.asInstanceOf[TransferParameter]

}
