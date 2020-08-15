package models.common

import models.`abstract`.TransactionMessage
import models.common.Serializable.{Coin, Properties, StdMsg, ValidatorDescription, CommissionRates => SerializableCommissionRates}
import play.api.Logger
import play.api.libs.json._
import utilities.MicroNumber

object TransactionMessages {

  private implicit val module: String = constants.Module.TRANSACTION_MESSAGE

  private implicit val logger: Logger = Logger(this.getClass)

  //bank
  case class SendCoin(fromAddress: String, toAddress: String, amounts: Seq[Coin]) extends TransactionMessage

  implicit val sendCoinReads: Reads[SendCoin] = Json.reads[SendCoin]

  //crisis
  case class VerifyInvariant(sender: String, invariantModuleName: String, invariantRoute: String) extends TransactionMessage

  implicit val verifyInvariantReads: Reads[VerifyInvariant] = Json.reads[VerifyInvariant]

  //distribution
  case class SetWithdrawAddress(delegatorAddress: String, withdrawAddress: String) extends TransactionMessage

  implicit val setWithdrawAddressReads: Reads[SetWithdrawAddress] = Json.reads[SetWithdrawAddress]

  case class WithdrawDelegatorReward(delegatorAddress: String, validatorAddress: String) extends TransactionMessage

  implicit val withdrawDelegatorRewardReads: Reads[WithdrawDelegatorReward] = Json.reads[WithdrawDelegatorReward]

  case class WithdrawValidatorCommission(validatorAddress: String) extends TransactionMessage

  implicit val withdrawValidatorCommissionReads: Reads[WithdrawValidatorCommission] = Json.reads[WithdrawValidatorCommission]

  case class FundCommunityPool(amount: Seq[Coin], depositor: String) extends TransactionMessage

  implicit val fundCommunityPoolReads: Reads[FundCommunityPool] = Json.reads[FundCommunityPool]

  //evidence
  //TODO Pending in cosmos-sdk

  //gov
  case class Deposit(proposalID: Long, depositor: String, amount: Seq[Coin]) extends TransactionMessage

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  case class ContentValue(title: String, description: String, recipient: String, amount: Seq[Coin])

  implicit val contentValueReads: Reads[ContentValue] = Json.reads[ContentValue]

  implicit val contentValueWrites: OWrites[ContentValue] = Json.writes[ContentValue]

  case class Content(value: ContentValue)

  implicit val contentReads: Reads[Content] = Json.reads[Content]

  implicit val contentWrites: OWrites[Content] = Json.writes[Content]

  case class SubmitProposal(content: Content, initialDeposit: Seq[Coin], proposer: String) extends TransactionMessage

  implicit val submitProposalReads: Reads[SubmitProposal] = Json.reads[SubmitProposal]

  case class Vote(proposalID: Long, voter: String, option: Int) extends TransactionMessage

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  //slashing
  case class Unjail(address: String) extends TransactionMessage

  implicit val unjailReads: Reads[Unjail] = Json.reads[Unjail]

  //staking
  case class Description(moniker: Option[String], identity: Option[String], website: Option[String], securityContact: Option[String], details: Option[String]) {
    def toSerializableDescription: ValidatorDescription = ValidatorDescription(moniker = moniker, identity = identity, website = website, securityContact = securityContact, details = details)
  }

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  implicit val descriptionWrites: OWrites[Description] = Json.writes[Description]

  case class Commission(rate: String, maxRate: String, maxChangeRate: String) {
    def toSerializableCommissionRates: SerializableCommissionRates = SerializableCommissionRates(rate = BigDecimal(rate) * 100, maxRate = BigDecimal(maxRate) * 100, maxChangeRate = BigDecimal(maxChangeRate) * 100)
  }

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

  implicit val commissionWrites: OWrites[Commission] = Json.writes[Commission]

  case class CreateValidator(delegatorAddress: String, validatorAddress: String, publicKey: String, value: Coin, minSelfDelegation: MicroNumber, commission: Commission, description: Description) extends TransactionMessage

  implicit val createValidatorReads: Reads[CreateValidator] = Json.reads[CreateValidator]

  case class EditValidator(validatorAddress: String, commissionRate: Option[String], description: Description, minSelfDelegation: Option[MicroNumber]) extends TransactionMessage

  implicit val editValidatorReads: Reads[EditValidator] = Json.reads[EditValidator]

  case class Delegate(delegatorAddress: String, validatorAddress: String, amount: Coin) extends TransactionMessage

  implicit val delegateReads: Reads[Delegate] = Json.reads[Delegate]

  case class Redelegate(delegatorAddress: String, validatorSrcAddress: String, validatorDstAddress: String, amount: Coin) extends TransactionMessage

  implicit val redelegateReads: Reads[Redelegate] = Json.reads[Redelegate]

  case class Undelegate(delegatorAddress: String, validatorAddress: String, amount: Coin) extends TransactionMessage

  implicit val undelegateReads: Reads[Undelegate] = Json.reads[Undelegate]

  //Asset
  case class AssetMint(from: String, fromID: String, toID: String, maintainersID: String, classificationID: String, properties: Properties, lock: Int, burn: Int) extends TransactionMessage

  implicit val assetMintReads: Reads[AssetMint] = Json.reads[AssetMint]

  case class AssetMutate(from: String, assetID: String, properties: Properties, lock: Int, burn: Int) extends TransactionMessage

  implicit val assetMutateReads: Reads[AssetMutate] = Json.reads[AssetMutate]

  case class AssetBurn(from: String, fromID: String, assetID: String) extends TransactionMessage

  implicit val assetBurnReads: Reads[AssetBurn] = Json.reads[AssetBurn]

  //Identity
  case class IdentityIssue(from: String, to: String, fromID: String, maintainersID: String, classificationID: String, properties: Properties) extends TransactionMessage

  implicit val identityIssueReads: Reads[IdentityIssue] = Json.reads[IdentityIssue]

  case class IdentityProvision(from: String, to: String, identityID: String) extends TransactionMessage

  implicit val identityProvisionReads: Reads[IdentityProvision] = Json.reads[IdentityProvision]

  case class IdentityUnprovision(from: String, to: String, identityID: String) extends TransactionMessage

  implicit val identityUnprovisionReads: Reads[IdentityUnprovision] = Json.reads[IdentityUnprovision]

  //Split
  case class SplitSend(from: String, fromID: String, toID: String, ownableID: String, split: BigDecimal) extends TransactionMessage

  implicit val splitSendReads: Reads[SplitSend] = Json.reads[SplitSend]

  case class SplitWrap(from: String, fromID: String, coins: Seq[Coin]) extends TransactionMessage

  implicit val splitWrapReads: Reads[SplitWrap] = Json.reads[SplitWrap]

  case class SplitUnwrap(from: String, fromID: String, ownableID: String, split: BigDecimal) extends TransactionMessage

  implicit val splitUnwrapReads: Reads[SplitUnwrap] = Json.reads[SplitUnwrap]

  //Order
  case class OrderMake(from: String, maintainersID: String, makerID: String, takerID: String, makerSplit: BigDecimal, makerSplitID: String, exchangeRate: BigDecimal, takerSplitID: String) extends TransactionMessage

  implicit val orderMakeReads: Reads[OrderMake] = Json.reads[OrderMake]

  case class OrderTake(from: String, fromID: String, takerSplit: BigDecimal, orderID: String) extends TransactionMessage

  implicit val orderTakeReads: Reads[OrderTake] = Json.reads[OrderTake]

  case class OrderCancel(from: String, orderID: String) extends TransactionMessage

  implicit val orderCancelReads: Reads[OrderCancel] = Json.reads[OrderCancel]

  //classification
  case class ClassificationDefine(from: String, fromID: String, maintainersID: String, traits: Seq[Serializable.Trait]) extends TransactionMessage

  implicit val classificationDefineReads: Reads[ClassificationDefine] = Json.reads[ClassificationDefine]

  //meta
  case class MetaReveal(from: String, fromID: String, data: String) extends TransactionMessage

  implicit val metaRevealReads: Reads[MetaReveal] = Json.reads[MetaReveal]

  //unknown
  case class Unknown(value: String) extends TransactionMessage

  implicit val unknownReads: Reads[Unknown] = Json.reads[Unknown]

  implicit val transactionMessageWrites: Writes[TransactionMessage] = {
    //bank
    case sendCoin: SendCoin => Json.toJson(sendCoin)(Json.writes[SendCoin])
    //staking
    case createValidator: CreateValidator => Json.toJson(createValidator)(Json.writes[CreateValidator])
    case editValidator: EditValidator => Json.toJson(editValidator)(Json.writes[EditValidator])
    case delegate: Delegate => Json.toJson(delegate)(Json.writes[Delegate])
    case redelegate: Redelegate => Json.toJson(redelegate)(Json.writes[Redelegate])
    case undelegate: Undelegate => Json.toJson(undelegate)(Json.writes[Undelegate])
    //crisis
    case verifyInvariant: VerifyInvariant => Json.toJson(verifyInvariant)(Json.writes[VerifyInvariant])
    //distribution
    case setWithdrawAddress: SetWithdrawAddress => Json.toJson(setWithdrawAddress)(Json.writes[SetWithdrawAddress])
    case withdrawDelegatorReward: WithdrawDelegatorReward => Json.toJson(withdrawDelegatorReward)(Json.writes[WithdrawDelegatorReward])
    case withdrawValidatorCommission: WithdrawValidatorCommission => Json.toJson(withdrawValidatorCommission)(Json.writes[WithdrawValidatorCommission])
    case fundCommunityPool: FundCommunityPool => Json.toJson(fundCommunityPool)(Json.writes[FundCommunityPool])
    //gov
    case deposit: Deposit => Json.toJson(deposit)(Json.writes[Deposit])
    case submitProposal: SubmitProposal => Json.toJson(submitProposal)(Json.writes[SubmitProposal])
    case vote: Vote => Json.toJson(vote)(Json.writes[Vote])
    //slashing
    case unjail: Unjail => Json.toJson(unjail)(Json.writes[Unjail])
    //asset
    case assetMint: AssetMint => Json.toJson(assetMint)(Json.writes[AssetMint])
    case assetMutate: AssetMutate => Json.toJson(assetMutate)(Json.writes[AssetMutate])
    case assetBurn: AssetBurn => Json.toJson(assetBurn)(Json.writes[AssetBurn])
    //Identity
    case identityIssue: IdentityIssue => Json.toJson(identityIssue)(Json.writes[IdentityIssue])
    case identityProvision: IdentityProvision => Json.toJson(identityProvision)(Json.writes[IdentityProvision])
    case identityUnprovision: IdentityUnprovision => Json.toJson(identityUnprovision)(Json.writes[IdentityUnprovision])
    //Split
    case splitSend: SplitSend => Json.toJson(splitSend)(Json.writes[SplitSend])
    case splitWrap: SplitWrap => Json.toJson(splitWrap)(Json.writes[SplitWrap])
    case splitUnwrap: SplitUnwrap => Json.toJson(splitUnwrap)(Json.writes[SplitUnwrap])
    //Order
    case orderMake: OrderMake => Json.toJson(orderMake)(Json.writes[OrderMake])
    case orderTake: OrderTake => Json.toJson(orderTake)(Json.writes[OrderTake])
    case orderCancel: OrderCancel => Json.toJson(orderCancel)(Json.writes[OrderCancel])
    //Classification
    case classificationDefine: ClassificationDefine => Json.toJson(classificationDefine)(Json.writes[ClassificationDefine])
    //meta
    case metaReveal: MetaReveal => Json.toJson(metaReveal)(Json.writes[MetaReveal])
    case x: Any => Json.toJson(x.toString)
  }

  def stdMsgApply(msgType: String, value: JsObject): StdMsg = {
    msgType match {
      //bank
      case constants.Blockchain.TransactionMessage.SEND_COIN => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SendCoin](value.toString))
      //crisis
      case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[VerifyInvariant](value.toString))
      //distribution
      case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SetWithdrawAddress](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawDelegatorReward](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawValidatorCommission](value.toString))
      case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[FundCommunityPool](value.toString))
      //TODO evidence Pending in cosmos-sdk
      //case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SubmitEvidence](value.toString))
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
      case constants.Blockchain.TransactionMessage.ASSET_MINT => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetMint](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_MUTATE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetMutate](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_BURN => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[AssetBurn](value.toString))
      //identity
      case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityIssue](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityProvision](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[IdentityUnprovision](value.toString))
      //split
      case constants.Blockchain.TransactionMessage.SPLIT_SEND => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitSend](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_WRAP => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitWrap](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[SplitUnwrap](value.toString))
      //order
      case constants.Blockchain.TransactionMessage.ORDER_MAKE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderMake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_TAKE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderTake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_CANCEL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[OrderCancel](value.toString))
      //classification
      case constants.Blockchain.TransactionMessage.CLASSIFICATION_DEFINE => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[ClassificationDefine](value.toString))
      //meta
      case constants.Blockchain.TransactionMessage.META_REVEAL => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[MetaReveal](value.toString))
      case _ => StdMsg(msgType, utilities.JSON.convertJsonStringToObject[Unknown](value.toString))
    }
  }


}