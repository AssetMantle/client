package models.common

import models.Abstract.Parameter
import models.common.Serializable.Coin
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, OWrites, Reads, Writes}

object Parameters {

  case class SlashingParameter(signedBlocksWindow: Int, minSignedPerWindow: BigDecimal, downtimeJailDuration: BigInt, slashFractionDoubleSign: BigDecimal, slashFractionDowntime: BigDecimal) extends Parameter

  implicit val slashingParameterWrites: OWrites[SlashingParameter] = Json.writes[SlashingParameter]

  implicit val slashingParameterReads: Reads[SlashingParameter] = Json.reads[SlashingParameter]

  case class StakingParameter(unbondingTime: BigInt, maxValidators: Int, maxEntries: Int, historicalEntries: Int, bondDenom: String) extends Parameter

  implicit val stakingParameterWrites: OWrites[StakingParameter] = Json.writes[StakingParameter]

  implicit val stakingParameterReads: Reads[StakingParameter] = Json.reads[StakingParameter]

  case class MintingParameter(mintDenom: String, inflationRateChange: BigDecimal, inflationMax: BigDecimal, inflationMin: BigDecimal, goalBonded: BigDecimal, blocksPerYear: Int) extends Parameter

  implicit val mintingParameterWrites: OWrites[MintingParameter] = Json.writes[MintingParameter]

  implicit val mintingParameterReads: Reads[MintingParameter] = Json.reads[MintingParameter]

  case class DistributionParameter(communityTax: BigDecimal, baseProposerReward: BigDecimal, bonusProposerReward: BigDecimal, withdrawAddrEnabled: Boolean) extends Parameter

  implicit val distributionParameterWrites: OWrites[DistributionParameter] = Json.writes[DistributionParameter]

  implicit val distributionParameterReads: Reads[DistributionParameter] = Json.reads[DistributionParameter]

  case class GovernanceParameter(minDeposit: Seq[Coin], maxDepositPeriod: BigInt, votingPeriod: BigInt, quorum: BigDecimal, threshold: BigDecimal, veto: BigDecimal) extends Parameter

  implicit val governanceParameterWrites: OWrites[GovernanceParameter] = Json.writes[GovernanceParameter]

  implicit val governanceParameterReads: Reads[GovernanceParameter] = Json.reads[GovernanceParameter]

  implicit val parameterWrites: Writes[Parameter] = {
    case stakingParameter: StakingParameter => Json.toJson(stakingParameter)
    case slashingParameter: SlashingParameter => Json.toJson(slashingParameter)
    case mintingParameter: MintingParameter => Json.toJson(mintingParameter)
    case distributionParameter: DistributionParameter => Json.toJson(distributionParameter)
    case governanceParameter: GovernanceParameter => Json.toJson(governanceParameter)
    case x: Any => Json.toJson(x.toString)
  }

  implicit val parameterReads: Reads[Parameter] = {
    Json.format[StakingParameter].map(x => x: Parameter) or
      Json.format[SlashingParameter].map(x => x: Parameter) or
      Json.format[MintingParameter].map(x => x: Parameter) or
      Json.format[DistributionParameter].map(x => x: Parameter) or
      Json.format[GovernanceParameter].map(x => x: Parameter)
  }
}
