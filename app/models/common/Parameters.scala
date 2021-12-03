package models.common

import models.Abstract.Parameter
import models.common.Serializable.Coin
import play.api.libs.functional.syntax.toAlternativeOps
import play.api.libs.json.{Json, OWrites, Reads, Writes}

object Parameters {

  case class AuthParameter(maxMemoCharacters: String, sigVerifyCostEd25519: String, sigVerifyCostSecp256k1: String, txSigLimit: String, txSizeCostPerByte: String) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.AUTH
  }

  implicit val authParameterWrites: OWrites[AuthParameter] = Json.writes[AuthParameter]

  implicit val authParameterReads: Reads[AuthParameter] = Json.reads[AuthParameter]

  case class SendEnabled(denom: String, enabled: Boolean)

  implicit val sendEnabledWrites: OWrites[SendEnabled] = Json.writes[SendEnabled]

  implicit val sendEnabledReads: Reads[SendEnabled] = Json.reads[SendEnabled]

  case class BankParameter(defaultSendEnabled: Boolean, sendEnabled: Seq[SendEnabled]) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.BANK
  }

  implicit val bankParameterWrites: OWrites[BankParameter] = Json.writes[BankParameter]

  implicit val bankParameterReads: Reads[BankParameter] = Json.reads[BankParameter]

  case class CrisisParameter(constantFee: Coin) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.CRISIS
  }

  implicit val crisisParameterWrites: OWrites[CrisisParameter] = Json.writes[CrisisParameter]

  implicit val crisisParameterReads: Reads[CrisisParameter] = Json.reads[CrisisParameter]

  case class DistributionParameter(communityTax: BigDecimal, baseProposerReward: BigDecimal, bonusProposerReward: BigDecimal, withdrawAddrEnabled: Boolean) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.DISTRIBUTION
  }

  implicit val distributionParameterWrites: OWrites[DistributionParameter] = Json.writes[DistributionParameter]

  implicit val distributionParameterReads: Reads[DistributionParameter] = Json.reads[DistributionParameter]

  case class GovernanceParameter(minDeposit: Seq[Coin], maxDepositPeriod: Long, votingPeriod: Long, quorum: BigDecimal, threshold: BigDecimal, vetoThreshold: BigDecimal) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.GOVERNANCE
  }

  implicit val governanceParameterWrites: OWrites[GovernanceParameter] = Json.writes[GovernanceParameter]

  implicit val governanceParameterReads: Reads[GovernanceParameter] = Json.reads[GovernanceParameter]

  case class HalvingParameter(blockHeight: Int) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.HALVING
  }

  implicit val halvingParameterWrites: OWrites[HalvingParameter] = Json.writes[HalvingParameter]

  implicit val halvingParameterReads: Reads[HalvingParameter] = Json.reads[HalvingParameter]

  case class IBCParameter(allowedClients: Seq[String]) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.IBC
  }

  implicit val ibcParameterWrites: OWrites[IBCParameter] = Json.writes[IBCParameter]

  implicit val ibcParameterReads: Reads[IBCParameter] = Json.reads[IBCParameter]

  case class MintingParameter(mintDenom: String, inflationRateChange: BigDecimal, inflationMax: BigDecimal, inflationMin: BigDecimal, goalBonded: BigDecimal, blocksPerYear: Int) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.MINT
  }

  implicit val mintingParameterWrites: OWrites[MintingParameter] = Json.writes[MintingParameter]

  implicit val mintingParameterReads: Reads[MintingParameter] = Json.reads[MintingParameter]

  case class SlashingParameter(signedBlocksWindow: Int, minSignedPerWindow: BigDecimal, downtimeJailDuration: Long, slashFractionDoubleSign: BigDecimal, slashFractionDowntime: BigDecimal) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.SLASHING
  }

  implicit val slashingParameterWrites: OWrites[SlashingParameter] = Json.writes[SlashingParameter]

  implicit val slashingParameterReads: Reads[SlashingParameter] = Json.reads[SlashingParameter]

  case class StakingParameter(unbondingTime: Long, maxValidators: Int, maxEntries: Int, historicalEntries: Int, bondDenom: String) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.STAKING
  }

  implicit val stakingParameterWrites: OWrites[StakingParameter] = Json.writes[StakingParameter]

  implicit val stakingParameterReads: Reads[StakingParameter] = Json.reads[StakingParameter]

  case class TransferParameter(receiveEnabled: Boolean, sendEnabled: Boolean) extends Parameter {
    val `type`: String = constants.Blockchain.ParameterType.TRANSFER
  }

  implicit val transferParameterWrites: OWrites[TransferParameter] = Json.writes[TransferParameter]

  implicit val transferParameterReads: Reads[TransferParameter] = Json.reads[TransferParameter]

  implicit val parameterWrites: Writes[Parameter] = {
    case authParameter: AuthParameter => Json.toJson(authParameter)
    case bankParameter: BankParameter => Json.toJson(bankParameter)
    case crisisParameter: CrisisParameter => Json.toJson(crisisParameter)
    case distributionParameter: DistributionParameter => Json.toJson(distributionParameter)
    case governanceParameter: GovernanceParameter => Json.toJson(governanceParameter)
    case halvingParameter: HalvingParameter => Json.toJson(halvingParameter)
    case ibcParameter: IBCParameter => Json.toJson(ibcParameter)
    case mintingParameter: MintingParameter => Json.toJson(mintingParameter)
    case slashingParameter: SlashingParameter => Json.toJson(slashingParameter)
    case stakingParameter: StakingParameter => Json.toJson(stakingParameter)
    case transferParameter: TransferParameter => Json.toJson(transferParameter)
    case x: Any => Json.toJson(x.toString)
  }

  implicit val parameterReads: Reads[Parameter] = {
    Json.format[AuthParameter].map(x => x: Parameter) or
      Json.format[BankParameter].map(x => x: Parameter) or
      Json.format[CrisisParameter].map(x => x: Parameter) or
      Json.format[DistributionParameter].map(x => x: Parameter) or
      Json.format[GovernanceParameter].map(x => x: Parameter) or
      Json.format[HalvingParameter].map(x => x: Parameter) or
      Json.format[IBCParameter].map(x => x: Parameter) or
      Json.format[MintingParameter].map(x => x: Parameter) or
      Json.format[SlashingParameter].map(x => x: Parameter) or
      Json.format[StakingParameter].map(x => x: Parameter) or
      Json.format[TransferParameter].map(x => x: Parameter)

  }
}
