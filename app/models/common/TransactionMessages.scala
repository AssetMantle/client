package models.common

import models.Abstract.{ProposalContent, TransactionMessage}
import models.common.Serializable._
import play.api.Logger
import play.api.libs.json._

object TransactionMessages {

  import models.common.ProposalContents._

  private implicit val module: String = constants.Module.TRANSACTION_MESSAGE

  private implicit val logger: Logger = Logger(this.getClass)

  //auth
  case class CreateVestingAccount(fromAddress: String, toAddress: String, amount: Seq[Coin], endTime: String, delayed: Boolean) extends TransactionMessage

  implicit val createVestingAccountReads: Reads[CreateVestingAccount] = Json.reads[CreateVestingAccount]

  implicit val createVestingAccountWrites: OWrites[CreateVestingAccount] = Json.writes[CreateVestingAccount]

  case class Input(address: String, coins: Seq[Coin])

  implicit val inputReads: Reads[Input] = Json.reads[Input]

  implicit val inputWrites: OWrites[Input] = Json.writes[Input]

  case class Output(address: String, coins: Seq[Coin])

  implicit val outputReads: Reads[Output] = Json.reads[Output]

  implicit val outputWrites: OWrites[Output] = Json.writes[Output]

  case class MultiSend(inputs: Seq[Input], outputs: Seq[Output]) extends TransactionMessage

  implicit val multiSendReads: Reads[MultiSend] = Json.reads[MultiSend]

  implicit val multiSendWrites: OWrites[MultiSend] = Json.writes[MultiSend]

  //bank
  case class SendCoin(fromAddress: String, toAddress: String, amount: Seq[Coin]) extends TransactionMessage

  implicit val sendCoinReads: Reads[SendCoin] = Json.reads[SendCoin]

  implicit val sendCoinWrites: OWrites[SendCoin] = Json.writes[SendCoin]

  //crisis
  case class VerifyInvariant(sender: String, invariantModuleName: String, invariantRoute: String) extends TransactionMessage

  implicit val verifyInvariantReads: Reads[VerifyInvariant] = Json.reads[VerifyInvariant]

  implicit val verifyInvariantWrites: OWrites[VerifyInvariant] = Json.writes[VerifyInvariant]

  //distribution
  case class SetWithdrawAddress(delegatorAddress: String, withdrawAddress: String) extends TransactionMessage

  implicit val setWithdrawAddressReads: Reads[SetWithdrawAddress] = Json.reads[SetWithdrawAddress]

  implicit val setWithdrawAddressWrites: OWrites[SetWithdrawAddress] = Json.writes[SetWithdrawAddress]

  case class WithdrawDelegatorReward(delegatorAddress: String, validatorAddress: String) extends TransactionMessage

  implicit val withdrawDelegatorRewardReads: Reads[WithdrawDelegatorReward] = Json.reads[WithdrawDelegatorReward]

  implicit val withdrawDelegatorRewardWrites: OWrites[WithdrawDelegatorReward] = Json.writes[WithdrawDelegatorReward]

  case class WithdrawValidatorCommission(validatorAddress: String) extends TransactionMessage

  implicit val withdrawValidatorCommissionReads: Reads[WithdrawValidatorCommission] = Json.reads[WithdrawValidatorCommission]

  implicit val withdrawValidatorCommissionWrites: OWrites[WithdrawValidatorCommission] = Json.writes[WithdrawValidatorCommission]

  case class FundCommunityPool(amount: Seq[Coin], depositor: String) extends TransactionMessage

  implicit val fundCommunityPoolReads: Reads[FundCommunityPool] = Json.reads[FundCommunityPool]

  implicit val fundCommunityPoolWrites: OWrites[FundCommunityPool] = Json.writes[FundCommunityPool]

  //evidence - TODO As evidence interface
  case class Equivocation(height: String, time: String, power: String, consensusAddress: String)

  implicit val equivocationReads: Reads[Equivocation] = Json.reads[Equivocation]

  implicit val equivocationWrites: OWrites[Equivocation] = Json.writes[Equivocation]

  case class SubmitEvidence(submitter: String, evidence: Equivocation) extends TransactionMessage

  implicit val submitEvidenceReads: Reads[SubmitEvidence] = Json.reads[SubmitEvidence]

  implicit val submitEvidenceWrites: OWrites[SubmitEvidence] = Json.writes[SubmitEvidence]

  //gov
  case class Deposit(proposalID: String, depositor: String, amount: Seq[Coin]) extends TransactionMessage

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  implicit val depositWrites: OWrites[Deposit] = Json.writes[Deposit]

  case class SubmitProposal(content: ProposalContent, initialDeposit: Seq[Coin], proposer: String) extends TransactionMessage

  implicit val submitProposalReads: Reads[SubmitProposal] = Json.reads[SubmitProposal]

  implicit val submitProposalWrites: OWrites[SubmitProposal] = Json.writes[SubmitProposal]

  case class Vote(proposalID: String, voter: String, option: String) extends TransactionMessage

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  implicit val voteWrites: OWrites[Vote] = Json.writes[Vote]

  //slashing
  case class Unjail(validatorAddress: String) extends TransactionMessage

  implicit val unjailReads: Reads[Unjail] = Json.reads[Unjail]

  implicit val unjailWrites: OWrites[Unjail] = Json.writes[Unjail]

  //staking
  case class CreateValidator(delegatorAddress: String, validatorAddress: String, publicKey: String, value: Coin, minSelfDelegation: String, commissionRates: Serializable.Validator.CommissionRates, description: Serializable.Validator.Description) extends TransactionMessage

  implicit val createValidatorReads: Reads[CreateValidator] = Json.reads[CreateValidator]

  implicit val createValidatorWrites: OWrites[CreateValidator] = Json.writes[CreateValidator]

  case class EditValidator(validatorAddress: String, commissionRate: String, description: Serializable.Validator.Description, minSelfDelegation: String) extends TransactionMessage

  implicit val editValidatorReads: Reads[EditValidator] = Json.reads[EditValidator]

  implicit val editValidatorWrites: OWrites[EditValidator] = Json.writes[EditValidator]

  case class Delegate(delegatorAddress: String, validatorAddress: String, amount: Coin) extends TransactionMessage

  implicit val delegateReads: Reads[Delegate] = Json.reads[Delegate]

  implicit val delegateWrites: OWrites[Delegate] = Json.writes[Delegate]

  case class Redelegate(delegatorAddress: String, validatorSrcAddress: String, validatorDstAddress: String, amount: Coin) extends TransactionMessage

  implicit val redelegateReads: Reads[Redelegate] = Json.reads[Redelegate]

  implicit val redelegateWrites: OWrites[Redelegate] = Json.writes[Redelegate]

  case class Undelegate(delegatorAddress: String, validatorAddress: String, amount: Coin) extends TransactionMessage

  implicit val undelegateReads: Reads[Undelegate] = Json.reads[Undelegate]

  implicit val undelegateWrites: OWrites[Undelegate] = Json.writes[Undelegate]

  //Asset
  case class AssetDefine(from: String, fromID: String, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessage

  implicit val assetDefineReads: Reads[AssetDefine] = Json.reads[AssetDefine]

  implicit val assetDefineWrites: OWrites[AssetDefine] = Json.writes[AssetDefine]

  case class AssetMint(from: String, fromID: String, toID: String, classificationID: String, immutableMetaProperties: MetaProperties, immutableProperties: Properties, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessage

  implicit val assetMintReads: Reads[AssetMint] = Json.reads[AssetMint]

  implicit val assetMintWrites: OWrites[AssetMint] = Json.writes[AssetMint]

  case class AssetMutate(from: String, fromID: String, assetID: String, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessage

  implicit val assetMutateReads: Reads[AssetMutate] = Json.reads[AssetMutate]

  implicit val assetMutateWrites: OWrites[AssetMutate] = Json.writes[AssetMutate]

  case class AssetBurn(from: String, fromID: String, assetID: String) extends TransactionMessage

  implicit val assetBurnReads: Reads[AssetBurn] = Json.reads[AssetBurn]

  implicit val assetBurnWrites: OWrites[AssetBurn] = Json.writes[AssetBurn]

  //Identity
  case class IdentityDefine(from: String, fromID: String, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessage

  implicit val identityDefineReads: Reads[IdentityDefine] = Json.reads[IdentityDefine]

  implicit val identityDefineWrites: OWrites[IdentityDefine] = Json.writes[IdentityDefine]

  case class IdentityIssue(from: String, to: String, fromID: String, classificationID: String, immutableMetaProperties: MetaProperties, immutableProperties: Properties, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessage

  implicit val identityIssueReads: Reads[IdentityIssue] = Json.reads[IdentityIssue]

  implicit val identityIssueWrites: OWrites[IdentityIssue] = Json.writes[IdentityIssue]

  case class IdentityProvision(from: String, to: String, identityID: String) extends TransactionMessage

  implicit val identityProvisionReads: Reads[IdentityProvision] = Json.reads[IdentityProvision]

  implicit val identityProvisionWrites: OWrites[IdentityProvision] = Json.writes[IdentityProvision]

  case class IdentityUnprovision(from: String, to: String, identityID: String) extends TransactionMessage

  implicit val identityUnprovisionReads: Reads[IdentityUnprovision] = Json.reads[IdentityUnprovision]

  implicit val identityUnprovisionWrites: OWrites[IdentityUnprovision] = Json.writes[IdentityUnprovision]

  case class IdentityNub(from: String, nubID: String) extends TransactionMessage

  implicit val identityNubReads: Reads[IdentityNub] = Json.reads[IdentityNub]

  implicit val identityNubWrites: OWrites[IdentityNub] = Json.writes[IdentityNub]

  //Split
  case class SplitSend(from: String, fromID: String, toID: String, ownableID: String, split: BigDecimal) extends TransactionMessage

  implicit val splitSendReads: Reads[SplitSend] = Json.reads[SplitSend]

  implicit val splitSendWrites: OWrites[SplitSend] = Json.writes[SplitSend]

  case class SplitWrap(from: String, fromID: String, coins: Seq[Coin]) extends TransactionMessage

  implicit val splitWrapReads: Reads[SplitWrap] = Json.reads[SplitWrap]

  implicit val splitWrapWrites: OWrites[SplitWrap] = Json.writes[SplitWrap]

  case class SplitUnwrap(from: String, fromID: String, ownableID: String, split: BigDecimal) extends TransactionMessage

  implicit val splitUnwrapReads: Reads[SplitUnwrap] = Json.reads[SplitUnwrap]

  implicit val splitUnwrapWrites: OWrites[SplitUnwrap] = Json.writes[SplitUnwrap]

  //Order
  case class OrderDefine(from: String, fromID: String, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessage

  implicit val orderDefineReads: Reads[OrderDefine] = Json.reads[OrderDefine]

  implicit val orderDefineWrites: OWrites[OrderDefine] = Json.writes[OrderDefine]

  case class OrderMake(from: String, fromID: String, classificationID: String, makerOwnableID: String, takerOwnableID: String, expiresIn: Int, makerOwnableSplit: BigDecimal, immutableMetaProperties: MetaProperties, immutableProperties: Properties, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessage

  implicit val orderMakeReads: Reads[OrderMake] = Json.reads[OrderMake]

  implicit val orderMakeWrites: OWrites[OrderMake] = Json.writes[OrderMake]

  case class OrderTake(from: String, fromID: String, takerOwnableSplit: BigDecimal, orderID: String) extends TransactionMessage

  implicit val orderTakeReads: Reads[OrderTake] = Json.reads[OrderTake]

  implicit val orderTakeWrites: OWrites[OrderTake] = Json.writes[OrderTake]

  case class OrderCancel(from: String, fromID: String, orderID: String) extends TransactionMessage

  implicit val orderCancelReads: Reads[OrderCancel] = Json.reads[OrderCancel]

  implicit val orderCancelWrites: OWrites[OrderCancel] = Json.writes[OrderCancel]

  //meta
  case class MetaReveal(from: String, metaFact: MetaFact) extends TransactionMessage

  implicit val metaRevealReads: Reads[MetaReveal] = Json.reads[MetaReveal]

  implicit val metaRevealWrites: OWrites[MetaReveal] = Json.writes[MetaReveal]

  //maintainer
  case class MaintainerDeputize(from: String, fromID: String, toID: String, classificationID: String, maintainedTraits: Properties, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean) extends TransactionMessage

  implicit val maintainerDeputizeReads: Reads[MaintainerDeputize] = Json.reads[MaintainerDeputize]

  implicit val maintainerDeputizeWrites: OWrites[MaintainerDeputize] = Json.writes[MaintainerDeputize]

  //unknown
  case class Unknown(value: String) extends TransactionMessage

  implicit val unknownReads: Reads[Unknown] = Json.reads[Unknown]

  implicit val unknownWrites: OWrites[Unknown] = Json.writes[Unknown]

  implicit val transactionMessageWrites: Writes[TransactionMessage] = {
    //auth
    case createVestingAccount: CreateVestingAccount => Json.toJson(createVestingAccount)
    //bank
    case sendCoin: SendCoin => Json.toJson(sendCoin)
    case multiSend: MultiSend => Json.toJson(multiSend)
    //crisis
    case verifyInvariant: VerifyInvariant => Json.toJson(verifyInvariant)
    //distribution
    case setWithdrawAddress: SetWithdrawAddress => Json.toJson(setWithdrawAddress)
    case withdrawDelegatorReward: WithdrawDelegatorReward => Json.toJson(withdrawDelegatorReward)
    case withdrawValidatorCommission: WithdrawValidatorCommission => Json.toJson(withdrawValidatorCommission)
    case fundCommunityPool: FundCommunityPool => Json.toJson(fundCommunityPool)
    //evidence
    case submitEvidence: SubmitEvidence => Json.toJson(submitEvidence)
    //staking
    case createValidator: CreateValidator => Json.toJson(createValidator)
    case editValidator: EditValidator => Json.toJson(editValidator)
    case delegate: Delegate => Json.toJson(delegate)
    case redelegate: Redelegate => Json.toJson(redelegate)
    case undelegate: Undelegate => Json.toJson(undelegate)
    //gov
    case deposit: Deposit => Json.toJson(deposit)
    case submitProposal: SubmitProposal => Json.toJson(submitProposal)
    case vote: Vote => Json.toJson(vote)
    //slashing
    case unjail: Unjail => Json.toJson(unjail)
    //asset
    case assetDefine: AssetDefine => Json.toJson(assetDefine)
    case assetMint: AssetMint => Json.toJson(assetMint)
    case assetMutate: AssetMutate => Json.toJson(assetMutate)
    case assetBurn: AssetBurn => Json.toJson(assetBurn)
    //Identity
    case identityDefine: IdentityDefine => Json.toJson(identityDefine)
    case identityIssue: IdentityIssue => Json.toJson(identityIssue)
    case identityProvision: IdentityProvision => Json.toJson(identityProvision)
    case identityUnprovision: IdentityUnprovision => Json.toJson(identityUnprovision)
    case identityNub: IdentityNub => Json.toJson(identityNub)
    //Split
    case splitSend: SplitSend => Json.toJson(splitSend)
    case splitWrap: SplitWrap => Json.toJson(splitWrap)
    case splitUnwrap: SplitUnwrap => Json.toJson(splitUnwrap)
    //Order
    case orderDefine: OrderDefine => Json.toJson(orderDefine)
    case orderMake: OrderMake => Json.toJson(orderMake)
    case orderTake: OrderTake => Json.toJson(orderTake)
    case orderCancel: OrderCancel => Json.toJson(orderCancel)
    //meta
    case metaReveal: MetaReveal => Json.toJson(metaReveal)
    //maintainer
    case deputize: MaintainerDeputize => Json.toJson(deputize)
    case x: Any => Json.toJson(x.toString)
  }

  def stdMsgApply(msgType: String, value: JsObject): StdMsg = {
    msgType match {
      //auth
      case constants.Blockchain.TransactionMessage.CREATE_VESTING_ACCOUNT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[CreateVestingAccount](value.toString))
      //bank
      case constants.Blockchain.TransactionMessage.SEND_COIN => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SendCoin](value.toString))
      case constants.Blockchain.TransactionMessage.MULTI_SEND => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[MultiSend](value.toString))
      //crisis
      case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[VerifyInvariant](value.toString))
      //distribution
      case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SetWithdrawAddress](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawDelegatorReward](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawValidatorCommission](value.toString))
      case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[FundCommunityPool](value.toString))
      //evidence
      case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SubmitEvidence](value.toString))
      //gov
      case constants.Blockchain.TransactionMessage.DEPOSIT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Deposit](value.toString))
      case constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SubmitProposal](value.toString))
      case constants.Blockchain.TransactionMessage.VOTE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Vote](value.toString))
      //slashing
      case constants.Blockchain.TransactionMessage.UNJAIL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Unjail](value.toString))
      //staking
      case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[CreateValidator](value.toString))
      case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[EditValidator](value.toString))
      case constants.Blockchain.TransactionMessage.DELEGATE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Delegate](value.toString))
      case constants.Blockchain.TransactionMessage.REDELEGATE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Redelegate](value.toString))
      case constants.Blockchain.TransactionMessage.UNDELEGATE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Undelegate](value.toString))
      //asset
      case constants.Blockchain.TransactionMessage.ASSET_DEFINE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetDefine](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_MINT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetMint](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_MUTATE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetMutate](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_BURN => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetBurn](value.toString))
      //identity
      case constants.Blockchain.TransactionMessage.IDENTITY_DEFINE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityDefine](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityIssue](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityProvision](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityUnprovision](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_NUB => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityNub](value.toString))
      //split
      case constants.Blockchain.TransactionMessage.SPLIT_SEND => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitSend](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_WRAP => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitWrap](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitUnwrap](value.toString))
      //order
      case constants.Blockchain.TransactionMessage.ORDER_DEFINE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderDefine](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_MAKE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderMake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_TAKE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderTake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_CANCEL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderCancel](value.toString))
      //meta
      case constants.Blockchain.TransactionMessage.META_REVEAL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[MetaReveal](value.toString))
      //maintainer
      case constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[MaintainerDeputize](value.toString))
      case _ => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Unknown](value.toString))
    }
  }


}