package models.common

import models.Abstract.Parameter
import models.common.Serializable.Coin
import play.api.libs.json.{Json, OWrites, Reads}

object Parameters {

  case class AuthParameter(maxMemoCharacters: String, sigVerifyCostEd25519: String, sigVerifyCostSecp256k1: String, txSigLimit: String, txSizeCostPerByte: String) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.AUTH
  }

  implicit val authParameterWrites: OWrites[AuthParameter] = Json.writes[AuthParameter]

  implicit val authParameterReads: Reads[AuthParameter] = Json.reads[AuthParameter]

  case class SendEnabled(denom: String, enabled: Boolean)

  implicit val sendEnabledWrites: OWrites[SendEnabled] = Json.writes[SendEnabled]

  implicit val sendEnabledReads: Reads[SendEnabled] = Json.reads[SendEnabled]

  case class BankParameter(defaultSendEnabled: Boolean, sendEnabled: Seq[SendEnabled]) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.BANK
  }

  implicit val bankParameterWrites: OWrites[BankParameter] = Json.writes[BankParameter]

  implicit val bankParameterReads: Reads[BankParameter] = Json.reads[BankParameter]

  case class CrisisParameter(constantFee: Coin) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.CRISIS
  }

  implicit val crisisParameterWrites: OWrites[CrisisParameter] = Json.writes[CrisisParameter]

  implicit val crisisParameterReads: Reads[CrisisParameter] = Json.reads[CrisisParameter]

  case class DistributionParameter(communityTax: BigDecimal, baseProposerReward: BigDecimal, bonusProposerReward: BigDecimal, withdrawAddrEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.DISTRIBUTION
  }

  implicit val distributionParameterWrites: OWrites[DistributionParameter] = Json.writes[DistributionParameter]

  implicit val distributionParameterReads: Reads[DistributionParameter] = Json.reads[DistributionParameter]

  case class GovernanceParameter(minDeposit: Seq[Coin], maxDepositPeriod: Long, votingPeriod: Long, quorum: BigDecimal, threshold: BigDecimal, vetoThreshold: BigDecimal) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.GOVERNANCE
  }

  implicit val governanceParameterWrites: OWrites[GovernanceParameter] = Json.writes[GovernanceParameter]

  implicit val governanceParameterReads: Reads[GovernanceParameter] = Json.reads[GovernanceParameter]

  case class IBCParameter(allowedClients: Seq[String]) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.IBC
  }

  implicit val ibcParameterWrites: OWrites[IBCParameter] = Json.writes[IBCParameter]

  implicit val ibcParameterReads: Reads[IBCParameter] = Json.reads[IBCParameter]

  case class MintingParameter(mintDenom: String, inflationRateChange: BigDecimal, inflationMax: BigDecimal, inflationMin: BigDecimal, goalBonded: BigDecimal, blocksPerYear: Int) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.MINT
  }

  implicit val mintingParameterWrites: OWrites[MintingParameter] = Json.writes[MintingParameter]

  implicit val mintingParameterReads: Reads[MintingParameter] = Json.reads[MintingParameter]

  case class SlashingParameter(signedBlocksWindow: Int, minSignedPerWindow: BigDecimal, downtimeJailDuration: Long, slashFractionDoubleSign: BigDecimal, slashFractionDowntime: BigDecimal) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.SLASHING
  }

  implicit val slashingParameterWrites: OWrites[SlashingParameter] = Json.writes[SlashingParameter]

  implicit val slashingParameterReads: Reads[SlashingParameter] = Json.reads[SlashingParameter]

  case class StakingParameter(unbondingTime: Long, maxValidators: Int, maxEntries: Int, historicalEntries: Int, bondDenom: String) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.STAKING
  }

  implicit val stakingParameterWrites: OWrites[StakingParameter] = Json.writes[StakingParameter]

  implicit val stakingParameterReads: Reads[StakingParameter] = Json.reads[StakingParameter]

  case class TransferParameter(receiveEnabled: Boolean, sendEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.TRANSFER
  }

  implicit val transferParameterWrites: OWrites[TransferParameter] = Json.writes[TransferParameter]

  implicit val transferParameterReads: Reads[TransferParameter] = Json.reads[TransferParameter]

  case class ClassificationParameter(bondRate: BigInt, maxPropertyCount: BigInt, defineEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.CLASSIFICATIONS
  }

  implicit val classificationParameterWrites: OWrites[ClassificationParameter] = Json.writes[ClassificationParameter]

  implicit val classificationParameterReads: Reads[ClassificationParameter] = Json.reads[ClassificationParameter]

  case class AssetParameter(burnEnabled: Boolean, mintEnabled: Boolean, renumerateEnabled: Boolean, wrapAllowedCoins: Seq[String], unwrapAllowedCoins: Seq[String]) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.ASSETS
  }

  implicit val AssetParameterWrites: OWrites[AssetParameter] = Json.writes[AssetParameter]

  implicit val AssetParameterReads: Reads[AssetParameter] = Json.reads[AssetParameter]

  case class IdentityParameter(issueEnabled: Boolean, maxProvisionAddressCount: BigInt, quashEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.IDENTITIES
  }

  implicit val IdentityParameterWrites: OWrites[IdentityParameter] = Json.writes[IdentityParameter]

  implicit val IdentityParameterReads: Reads[IdentityParameter] = Json.reads[IdentityParameter]

  case class MaintainerParameter(deputizeAllowed: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.MAINTAINERS
  }

  implicit val MaintainerParameterWrites: OWrites[MaintainerParameter] = Json.writes[MaintainerParameter]

  implicit val MaintainerParameterReads: Reads[MaintainerParameter] = Json.reads[MaintainerParameter]

  case class MetaParameter(revealEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.METAS
  }

  implicit val MetaParameterWrites: OWrites[MetaParameter] = Json.writes[MetaParameter]

  implicit val MetaParameterReads: Reads[MetaParameter] = Json.reads[MetaParameter]

  case class OrderParameter(maxOrderLife: Long, putEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.ORDERS
  }

  implicit val OrderParameterWrites: OWrites[OrderParameter] = Json.writes[OrderParameter]

  implicit val OrderParameterReads: Reads[OrderParameter] = Json.reads[OrderParameter]

  case class SplitParameter(transferEnabled: Boolean) extends Parameter {
    val parameterType: String = constants.Blockchain.ParameterType.SPLITS
  }

  implicit val SplitParameterWrites: OWrites[SplitParameter] = Json.writes[SplitParameter]

  implicit val SplitParameterReads: Reads[SplitParameter] = Json.reads[SplitParameter]
}
