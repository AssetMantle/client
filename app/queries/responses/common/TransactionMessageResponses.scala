package queries.responses.common

import exceptions.BaseException
import models.Abstract.TransactionMessage
import models.common.{Serializable, TransactionMessages}
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.Abstract.TransactionMessageResponse
import queries.responses.TransactionResponse.Msg
import utilities.MicroNumber

object TransactionMessageResponses {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_RESPONSES

  implicit val logger: Logger = Logger(this.getClass)

  case class Input(address: String, coins: Seq[Coin]) {
    def toInput: TransactionMessages.Input = TransactionMessages.Input(address = address, coins = coins.map(_.toCoin))
  }

  implicit val inputReads: Reads[Input] = Json.reads[Input]

  case class Output(address: String, coins: Seq[Coin]) {
    def toOutput: TransactionMessages.Output = TransactionMessages.Output(address = address, coins = coins.map(_.toCoin))
  }

  implicit val outputReads: Reads[Output] = Json.reads[Output]

  case class MultiSend(inputs: Seq[Input], outputs: Seq[Output]) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.MultiSend(inputs = inputs.map(_.toInput), outputs = outputs.map(_.toOutput))
  }

  implicit val multiSendReads: Reads[MultiSend] = Json.reads[MultiSend]

  //  bank
  case class SendCoin(from_address: String, to_address: String, amount: Seq[Coin]) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SendCoin(fromAddress = from_address, toAddress = to_address, amounts = amount.map(x => Serializable.Coin(denom = x.denom, amount = x.amount)))
  }

  implicit val sendCoinReads: Reads[SendCoin] = Json.reads[SendCoin]

  //crisis
  case class VerifyInvariant(sender: String, invariant_module_name: String, invariant_route: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.VerifyInvariant(sender = sender, invariantModuleName = invariant_module_name, invariantRoute = invariant_route)
  }

  implicit val verifyInvariantReads: Reads[VerifyInvariant] = Json.reads[VerifyInvariant]

  //distribution
  case class SetWithdrawAddress(delegator_address: String, withdraw_address: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SetWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address)
  }

  implicit val setWithdrawAddressReads: Reads[SetWithdrawAddress] = Json.reads[SetWithdrawAddress]

  case class WithdrawDelegatorReward(delegator_address: String, validator_address: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.WithdrawDelegatorReward(delegatorAddress = delegator_address, validatorAddress = validator_address)
  }

  implicit val withdrawDelegatorRewardReads: Reads[WithdrawDelegatorReward] = Json.reads[WithdrawDelegatorReward]

  case class WithdrawValidatorCommission(validator_address: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.WithdrawValidatorCommission(validatorAddress = validator_address)
  }

  implicit val withdrawValidatorCommissionReads: Reads[WithdrawValidatorCommission] = Json.reads[WithdrawValidatorCommission]

  case class FundCommunityPool(amount: Seq[Coin], depositor: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.FundCommunityPool(amount = amount.map(_.toCoin), depositor = depositor)
  }

  implicit val fundCommunityPoolReads: Reads[FundCommunityPool] = Json.reads[FundCommunityPool]

  //gov
  case class Deposit(proposal_id: String, depositor: String, amount: Seq[Coin]) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Deposit(proposalID = proposal_id, depositor = depositor, amount = amount.map(_.toCoin))
  }

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  case class ContentValue(title: String, description: String) {
    def toContentValue: TransactionMessages.ContentValue = TransactionMessages.ContentValue(title = title, description = description)
  }

  implicit val contentValueReads: Reads[ContentValue] = Json.reads[ContentValue]

  case class Content(value: ContentValue) {
    def toContent: TransactionMessages.Content = TransactionMessages.Content(value = value.toContentValue)
  }

  implicit val contentReads: Reads[Content] = Json.reads[Content]

  case class SubmitProposal(content: Content, initial_deposit: Seq[Coin], proposer: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SubmitProposal(content = content.toContent, initialDeposit = initial_deposit.map(_.toCoin), proposer = proposer)
  }

  implicit val submitProposalReads: Reads[SubmitProposal] = Json.reads[SubmitProposal]

  case class Vote(proposal_id: String, voter: String, option: Int) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Vote(proposalID = proposal_id, voter = voter, option = option)
  }

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  //slashing
  case class Unjail(address: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Unjail(address = address)
  }

  implicit val unjailReads: Reads[Unjail] = Json.reads[Unjail]

  //staking
  case class Description(moniker: Option[String], identity: Option[String], website: Option[String], security_contact: Option[String], details: Option[String]) {
    def toDescription: TransactionMessages.Description = TransactionMessages.Description(moniker = moniker, identity = identity, website = website, securityContact = security_contact, details = details)
  }

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  case class Commission(rate: Option[String], max_rate: Option[String], max_change_rate: Option[String]) {
    def toCommission: TransactionMessages.Commission = TransactionMessages.Commission(rate = rate.getOrElse("0.0"), maxRate = max_rate.getOrElse("0.0"), maxChangeRate = max_change_rate.getOrElse("0.0"))
  }

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

  case class CreateValidator(delegator_address: String, validator_address: String, pubkey: String, value: Coin, commission: Commission, description: Description, min_self_delegation: MicroNumber) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.CreateValidator(delegatorAddress = delegator_address, validatorAddress = validator_address, publicKey = pubkey, value = Serializable.Coin(denom = value.denom, amount = value.amount), commission = commission.toCommission, description = description.toDescription, minSelfDelegation = min_self_delegation)
  }

  implicit val createValidatorReads: Reads[CreateValidator] = Json.reads[CreateValidator]

  case class EditValidator(validator_address: String, commission_rate: Option[String], description: Description, min_self_delegation: Option[MicroNumber]) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.EditValidator(validatorAddress = validator_address, commissionRate = commission_rate, description = description.toDescription, minSelfDelegation = min_self_delegation)
  }

  implicit val editValidatorReads: Reads[EditValidator] = Json.reads[EditValidator]

  case class Delegate(delegator_address: String, validator_address: String, amount: Coin) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Delegate(delegatorAddress = delegator_address, validatorAddress = validator_address, amount = amount.toCoin)
  }

  implicit val delegateReads: Reads[Delegate] = Json.reads[Delegate]

  case class Redelegate(delegator_address: String, validator_src_address: String, validator_dst_address: String, amount: Coin) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Redelegate(delegatorAddress = delegator_address, validatorSrcAddress = validator_src_address, validatorDstAddress = validator_dst_address, amount = amount.toCoin)
  }

  implicit val redelegateReads: Reads[Redelegate] = Json.reads[Redelegate]

  case class Undelegate(delegator_address: String, validator_address: String, amount: Coin) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Undelegate(delegatorAddress = delegator_address, validatorAddress = validator_address, amount = amount.toCoin)
  }

  implicit val undelegateReads: Reads[Undelegate] = Json.reads[Undelegate]

  //Asset
  case class AssetDefine(from: String, fromID: ID, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.AssetDefine(from = from, fromID = fromID.value.idString, immutableMetaTraits = immutableMetaTraits.toMetaProperties, immutableTraits = immutableTraits.toProperties, mutableMetaTraits = mutableMetaTraits.toMetaProperties, mutableTraits = mutableTraits.toProperties)
  }

  implicit val assetDefineReads: Reads[AssetDefine] = Json.reads[AssetDefine]

  case class AssetMint(from: String, fromID: ID, toID: ID, classificationID: ID, immutableMetaProperties: MetaProperties, mutableMetaProperties: MetaProperties, mutableProperties: Properties, immutableProperties: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.AssetMint(from = from, fromID = fromID.value.idString, toID = toID.value.idString, classificationID = classificationID.value.idString, immutableMetaProperties = immutableMetaProperties.toMetaProperties, mutableMetaProperties = mutableMetaProperties.toMetaProperties, mutableProperties = mutableProperties.toProperties, immutableProperties = immutableProperties.toProperties)
  }

  implicit val assetMintReads: Reads[AssetMint] = Json.reads[AssetMint]

  case class AssetMutate(from: String, fromID: ID, assetID: ID, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.AssetMutate(from = from, fromID = fromID.value.idString, assetID = assetID.value.idString, mutableMetaProperties = mutableMetaProperties.toMetaProperties, mutableProperties = mutableProperties.toProperties)
  }

  implicit val assetMutateReads: Reads[AssetMutate] = Json.reads[AssetMutate]

  case class AssetBurn(from: String, fromID: ID, assetID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.AssetBurn(from = from, assetID = assetID.value.idString, fromID = fromID.value.idString)
  }

  implicit val assetBurnReads: Reads[AssetBurn] = Json.reads[AssetBurn]

  //Identity
  case class IdentityDefine(from: String, fromID: ID, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.IdentityDefine(from = from, fromID = fromID.value.idString, immutableMetaTraits = immutableMetaTraits.toMetaProperties, immutableTraits = immutableTraits.toProperties, mutableMetaTraits = mutableMetaTraits.toMetaProperties, mutableTraits = mutableTraits.toProperties)
  }

  implicit val identityDefineReads: Reads[IdentityDefine] = Json.reads[IdentityDefine]

  case class IdentityIssue(from: String, to: String, fromID: ID, classificationID: ID, immutableMetaProperties: MetaProperties, mutableMetaProperties: MetaProperties, mutableProperties: Properties, immutableProperties: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.IdentityIssue(from = from, to = to, fromID = fromID.value.idString, classificationID = classificationID.value.idString, immutableMetaProperties = immutableMetaProperties.toMetaProperties, mutableMetaProperties = mutableMetaProperties.toMetaProperties, mutableProperties = mutableProperties.toProperties, immutableProperties = immutableProperties.toProperties)
  }

  implicit val identityIssueReads: Reads[IdentityIssue] = Json.reads[IdentityIssue]

  case class IdentityProvision(from: String, to: String, identityID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.IdentityProvision(from = from, to = to, identityID = identityID.value.idString)
  }

  implicit val identityProvisionReads: Reads[IdentityProvision] = Json.reads[IdentityProvision]

  case class IdentityUnprovision(from: String, to: String, identityID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.IdentityUnprovision(from = from, to = to, identityID = identityID.value.idString)
  }

  implicit val identityUnprovisionReads: Reads[IdentityUnprovision] = Json.reads[IdentityUnprovision]

  case class IdentityNub(from: String, nubID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.IdentityNub(from = from, nubID = nubID.value.idString)
  }

  implicit val identityNubReads: Reads[IdentityNub] = Json.reads[IdentityNub]

  //Split
  case class SplitSend(from: String, fromID: ID, toID: ID, ownableID: ID, split: BigDecimal) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SplitSend(from = from, fromID = fromID.value.idString, toID = toID.value.idString, ownableID = ownableID.value.idString, split = split)
  }

  implicit val splitSendReads: Reads[SplitSend] = Json.reads[SplitSend]

  case class SplitWrap(from: String, fromID: ID, coins: Seq[Coin]) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SplitWrap(from = from, fromID = fromID.value.idString, coins = coins.map(_.toCoin))
  }

  implicit val splitWrapReads: Reads[SplitWrap] = Json.reads[SplitWrap]

  case class SplitUnwrap(from: String, fromID: ID, ownableID: ID, split: BigDecimal) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.SplitUnwrap(from = from, fromID = fromID.value.idString, ownableID = ownableID.value.idString, split = split)
  }

  implicit val splitUnwrapReads: Reads[SplitUnwrap] = Json.reads[SplitUnwrap]

  //Order
  case class OrderDefine(from: String, fromID: ID, immutableMetaTraits: MetaProperties, immutableTraits: Properties, mutableMetaTraits: MetaProperties, mutableTraits: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.OrderDefine(from = from, fromID = fromID.value.idString, immutableMetaTraits = immutableMetaTraits.toMetaProperties, immutableTraits = immutableTraits.toProperties, mutableMetaTraits = mutableMetaTraits.toMetaProperties, mutableTraits = mutableTraits.toProperties)
  }

  implicit val orderDefineReads: Reads[OrderDefine] = Json.reads[OrderDefine]

  case class OrderMake(from: String, fromID: ID, classificationID: ID, makerOwnableID: ID, takerOwnableID: ID, expiresIn: Height, makerOwnableSplit: BigDecimal, immutableMetaProperties: MetaProperties, immutableProperties: Properties, mutableMetaProperties: MetaProperties, mutableProperties: Properties) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.OrderMake(from = from, fromID = fromID.value.idString, classificationID = classificationID.value.idString, makerOwnableID = makerOwnableID.value.idString, takerOwnableID = takerOwnableID.value.idString, expiresIn = expiresIn.toInt, makerOwnableSplit = makerOwnableSplit, immutableMetaProperties = immutableMetaProperties.toMetaProperties, immutableProperties = immutableProperties.toProperties, mutableMetaProperties = mutableMetaProperties.toMetaProperties, mutableProperties = mutableProperties.toProperties)
  }

  implicit val orderMakeReads: Reads[OrderMake] = Json.reads[OrderMake]

  case class OrderTake(from: String, fromID: ID, takerOwnableSplit: BigDecimal, orderID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.OrderTake(from = from, fromID = fromID.value.idString, takerOwnableSplit = takerOwnableSplit, orderID = orderID.value.idString)
  }

  implicit val orderTakeReads: Reads[OrderTake] = Json.reads[OrderTake]

  case class OrderCancel(from: String, fromID: ID, orderID: ID) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.OrderCancel(from = from, fromID = fromID.value.idString, orderID = orderID.value.idString)
  }

  implicit val orderCancelReads: Reads[OrderCancel] = Json.reads[OrderCancel]

  //meta
  case class MetaReveal(from: String, metaFact: MetaFact) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.MetaReveal(from = from, metaFact = metaFact.toMetaFact)
  }

  implicit val metaRevealReads: Reads[MetaReveal] = Json.reads[MetaReveal]

  //maintainer
  case class MaintainerDeputize(from: String, fromID: ID, toID: ID, classificationID: ID, maintainedTraits: Properties, addMaintainer: Boolean, removeMaintainer: Boolean, mutateMaintainer: Boolean) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.MaintainerDeputize(from = from, fromID = fromID.value.idString, toID = toID.value.idString, classificationID = classificationID.value.idString, maintainedTraits = maintainedTraits.toProperties, addMaintainer = addMaintainer, removeMaintainer = removeMaintainer, mutateMaintainer = mutateMaintainer)
  }

  implicit val maintainerDeputizeReads: Reads[MaintainerDeputize] = Json.reads[MaintainerDeputize]

  //unknown
  case class Unknown(value: String) extends TransactionMessageResponse {
    def toTxMsg: TransactionMessage = TransactionMessages.Unknown(value)
  }

  implicit val unknownReads: Reads[Unknown] = Json.reads[Unknown]

  def msgApply(msgType: String, value: JsObject): Msg = try {
    msgType match {
      //bank
      case constants.Blockchain.TransactionMessage.SEND_COIN => Msg(msgType, utilities.JSON.convertJsonStringToObject[SendCoin](value.toString))
      case constants.Blockchain.TransactionMessage.MULTI_SEND => Msg(msgType, utilities.JSON.convertJsonStringToObject[MultiSend](value.toString))
      //crisis
      case constants.Blockchain.TransactionMessage.VERIFY_INVARIANT => Msg(msgType, utilities.JSON.convertJsonStringToObject[VerifyInvariant](value.toString))
      //distribution
      case constants.Blockchain.TransactionMessage.SET_WITHDRAW_ADDRESS => Msg(msgType, utilities.JSON.convertJsonStringToObject[SetWithdrawAddress](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => Msg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawDelegatorReward](value.toString))
      case constants.Blockchain.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => Msg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawValidatorCommission](value.toString))
      case constants.Blockchain.TransactionMessage.FUND_COMMUNITY_POOL => Msg(msgType, utilities.JSON.convertJsonStringToObject[FundCommunityPool](value.toString))
      //TODO evidence Pending in cosmos-sdk
      //case constants.Blockchain.TransactionMessage.SUBMIT_EVIDENCE => Msg(msgType, utilities.JSON.convertJsonStringToObject[SubmitEvidence](value.toString))
      //gov
      case constants.Blockchain.TransactionMessage.DEPOSIT => Msg(msgType, utilities.JSON.convertJsonStringToObject[Deposit](value.toString))
      case constants.Blockchain.TransactionMessage.SUBMIT_PROPOSAL => Msg(msgType, utilities.JSON.convertJsonStringToObject[SubmitProposal](value.toString))
      case constants.Blockchain.TransactionMessage.VOTE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Vote](value.toString))
      //slashing
      case constants.Blockchain.TransactionMessage.UNJAIL => Msg(msgType, utilities.JSON.convertJsonStringToObject[Unjail](value.toString))
      //staking
      case constants.Blockchain.TransactionMessage.CREATE_VALIDATOR => Msg(msgType, utilities.JSON.convertJsonStringToObject[CreateValidator](value.toString))
      case constants.Blockchain.TransactionMessage.EDIT_VALIDATOR => Msg(msgType, utilities.JSON.convertJsonStringToObject[EditValidator](value.toString))
      case constants.Blockchain.TransactionMessage.DELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Delegate](value.toString))
      case constants.Blockchain.TransactionMessage.REDELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Redelegate](value.toString))
      case constants.Blockchain.TransactionMessage.UNDELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Undelegate](value.toString))
      //asset
      case constants.Blockchain.TransactionMessage.ASSET_DEFINE => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetDefine](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_MINT => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetMint](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_MUTATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetMutate](value.toString))
      case constants.Blockchain.TransactionMessage.ASSET_BURN => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetBurn](value.toString))
      //identity
      case constants.Blockchain.TransactionMessage.IDENTITY_DEFINE => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityDefine](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_ISSUE => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityIssue](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_PROVISION => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityProvision](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_UNPROVISION => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityUnprovision](value.toString))
      case constants.Blockchain.TransactionMessage.IDENTITY_NUB => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityNub](value.toString))
      //split
      case constants.Blockchain.TransactionMessage.SPLIT_SEND => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitSend](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_WRAP => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitWrap](value.toString))
      case constants.Blockchain.TransactionMessage.SPLIT_UNWRAP => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitUnwrap](value.toString))
      //order
      case constants.Blockchain.TransactionMessage.ORDER_DEFINE => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderDefine](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_MAKE => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderMake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_TAKE => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderTake](value.toString))
      case constants.Blockchain.TransactionMessage.ORDER_CANCEL => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderCancel](value.toString))
      //meta
      case constants.Blockchain.TransactionMessage.META_REVEAL => Msg(msgType, utilities.JSON.convertJsonStringToObject[MetaReveal](value.toString))
      //maintainer
      case constants.Blockchain.TransactionMessage.MAINTAINER_DEPUTIZE => Msg(msgType, utilities.JSON.convertJsonStringToObject[MaintainerDeputize](value.toString))
      //unknown
      case _ => Msg(msgType, utilities.JSON.convertJsonStringToObject[Unknown](value.toString))
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.TRANSACTION_STRUCTURE_CHANGED)
  }
}
