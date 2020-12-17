package models.Abstract

import models.common.Parameters._

abstract class Parameter {
  def asSlashingParameter: SlashingParameter = this.asInstanceOf[SlashingParameter]

  def asStakingParameter: StakingParameter = this.asInstanceOf[StakingParameter]

  def asDistributionParameter: DistributionParameter = this.asInstanceOf[DistributionParameter]

  def asMintingParameter: MintingParameter = this.asInstanceOf[MintingParameter]

  def asGovernanceParameter: GovernanceParameter = this.asInstanceOf[GovernanceParameter]
}
