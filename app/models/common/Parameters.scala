package models.common

import models.Abstract.Parameter
import models.common.Serializable.Coin
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, OWrites, Reads, Writes}

object Parameters {

  case class AuthParameter(maxMemoCharacters: String, sigVerifyCostEd25519: String, sigVerifyCostSecp256k1: String, txSigLimit: String, txSizeCostPerByte: String) extends Parameter

  implicit val authParameterWrites: OWrites[AuthParameter] = Json.writes[AuthParameter]

  implicit val authParameterReads: Reads[AuthParameter] = Json.reads[AuthParameter]

  case class SlashingParameter(signedBlocksWindow: String, minSignedPerWindow: String, downtimeJailDuration: String, slashFractionDoubleSign: String, slashFractionDowntime: String) extends Parameter

  implicit val slashingParameterWrites: OWrites[SlashingParameter] = Json.writes[SlashingParameter]

  implicit val slashingParameterReads: Reads[SlashingParameter] = Json.reads[SlashingParameter]

  case class StakingParameter(unbondingTime: String, maxValidators: Int, maxEntries: Int, historicalEntries: Int, bondDenom: String) extends Parameter

  implicit val stakingParameterWrites: OWrites[StakingParameter] = Json.writes[StakingParameter]

  implicit val stakingParameterReads: Reads[StakingParameter] = Json.reads[StakingParameter]

  case class MintingParameter(mintDenom: String, inflationRateChange: String, inflationMax: String, inflationMin: String, goalBonded: String, blocksPerYear: Int) extends Parameter

  implicit val mintingParameterWrites: OWrites[MintingParameter] = Json.writes[MintingParameter]

  implicit val mintingParameterReads: Reads[MintingParameter] = Json.reads[MintingParameter]

  case class DistributionParameter(communityTax: String, baseProposerReward: String, bonusProposerReward: String, withdrawAddrEnabled: Boolean) extends Parameter

  implicit val distributionParameterWrites: OWrites[DistributionParameter] = Json.writes[DistributionParameter]

  implicit val distributionParameterReads: Reads[DistributionParameter] = Json.reads[DistributionParameter]

  case class GovernanceParameter(minDeposit: Seq[Coin], maxDepositPeriod: BigInt, votingPeriod: BigInt, quorum: String, threshold: String, vetoThreshold: String) extends Parameter

  implicit val governanceParameterWrites: OWrites[GovernanceParameter] = Json.writes[GovernanceParameter]

  implicit val governanceParameterReads: Reads[GovernanceParameter] = Json.reads[GovernanceParameter]

  implicit val parameterWrites: Writes[Parameter] = {
    case stakingParameter: StakingParameter => Json.toJson(stakingParameter)
    case slashingParameter: SlashingParameter => Json.toJson(slashingParameter)
    case mintingParameter: MintingParameter => Json.toJson(mintingParameter)
    case distributionParameter: DistributionParameter => Json.toJson(distributionParameter)
    case governanceParameter: GovernanceParameter => Json.toJson(governanceParameter)
    case authParameter: AuthParameter => Json.toJson(authParameter)
    case x: Any => Json.toJson(x.toString)
  }

  implicit val parameterReads: Reads[Parameter] = {
    Json.format[StakingParameter].map(x => x: Parameter) or
      Json.format[SlashingParameter].map(x => x: Parameter) or
      Json.format[MintingParameter].map(x => x: Parameter) or
      Json.format[DistributionParameter].map(x => x: Parameter) or
      Json.format[GovernanceParameter].map(x => x: Parameter) or
      Json.format[AuthParameter].map(x => x: Parameter)
  }
}
