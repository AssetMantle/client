package queries.responses.common

import exceptions.BaseException
import models.common.Serializable.StdMsg
import models.common.{Serializable, TransactionMessages}
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.`abstract`.TransactionMessageResponse
import queries.responses.TransactionResponse.Msg
import utilities.MicroNumber

object TransactionMessageResponses {

  implicit val module: String = constants.Module.TRANSACTION_MESSAGE_RESPONSES

  implicit val logger: Logger = Logger(this.getClass)

  //  bank
  case class SendCoin(from_address: String, to_address: String, amount: Seq[Coin]) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SEND_COIN, TransactionMessages.SendCoin(fromAddress = from_address, toAddress = to_address, amounts = amount.map(x => Serializable.Coin(denom = x.denom, amount = x.amount))))
  }

  implicit val sendCoinReads: Reads[SendCoin] = Json.reads[SendCoin]

  //crisis
  case class VerifyInvariant(sender: String, invariant_module_name: String, invariant_route: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.VERIFY_INVARIANT, TransactionMessages.VerifyInvariant(sender = sender, invariantModuleName = invariant_module_name, invariantRoute = invariant_route))
  }

  implicit val verifyInvariantReads: Reads[VerifyInvariant] = Json.reads[VerifyInvariant]

  //distribution
  case class SetWithdrawAddress(delegator_address: String, withdraw_address: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SET_WITHDRAW_ADDRESS, TransactionMessages.SetWithdrawAddress(delegatorAddress = delegator_address, withdrawAddress = withdraw_address))
  }

  implicit val setWithdrawAddressReads: Reads[SetWithdrawAddress] = Json.reads[SetWithdrawAddress]

  case class WithdrawDelegatorReward(delegator_address: String, validator_address: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.WITHDRAW_DELEGATOR_REWARD, TransactionMessages.WithdrawDelegatorReward(delegatorAddress = delegator_address, validatorAddress = validator_address))
  }

  implicit val withdrawDelegatorRewardReads: Reads[WithdrawDelegatorReward] = Json.reads[WithdrawDelegatorReward]

  case class WithdrawValidatorCommission(validator_address: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION, TransactionMessages.WithdrawValidatorCommission(validatorAddress = validator_address))
  }

  implicit val withdrawValidatorCommissionReads: Reads[WithdrawValidatorCommission] = Json.reads[WithdrawValidatorCommission]

  case class FundCommunityPool(amount: Seq[Coin], depositor: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.FUND_COMMUNITY_POOL, TransactionMessages.FundCommunityPool(amount = amount.map(_.toCoin), depositor = depositor))
  }

  implicit val fundCommunityPoolReads: Reads[FundCommunityPool] = Json.reads[FundCommunityPool]

  //gov
  case class Deposit(proposal_id: Long, depositor: String, amount: Seq[Coin]) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.DEPOSIT, TransactionMessages.Deposit(proposalID = proposal_id, depositor = depositor, amount = amount.map(_.toCoin)))
  }

  implicit val depositReads: Reads[Deposit] = Json.reads[Deposit]

  case class ContentValue(title: String, description: String, recipient: String, amount: Seq[Coin]) {
    def toContentValue: TransactionMessages.ContentValue = TransactionMessages.ContentValue(title = title, description = description, recipient = recipient, amount = amount.map(_.toCoin))
  }

  implicit val contentValueReads: Reads[ContentValue] = Json.reads[ContentValue]

  case class Content(value: ContentValue) {
    def toContent: TransactionMessages.Content = TransactionMessages.Content(value = value.toContentValue)
  }

  implicit val contentReads: Reads[Content] = Json.reads[Content]

  case class SubmitProposal(content: Content, initial_deposit: Seq[Coin], proposer: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SUBMIT_PROPOSAL, TransactionMessages.SubmitProposal(content = content.toContent, initialDeposit = initial_deposit.map(_.toCoin), proposer = proposer))
  }

  implicit val submitProposalReads: Reads[SubmitProposal] = Json.reads[SubmitProposal]

  case class Vote(proposal_id: Long, voter: String, option: Int) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.VOTE, TransactionMessages.Vote(proposalID = proposal_id, voter = voter, option = option))
  }

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  //slashing
  case class Unjail(address: String) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.UNJAIL, TransactionMessages.Unjail(address = address))
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
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.CREATE_VALIDATOR, TransactionMessages.CreateValidator(delegatorAddress = delegator_address, validatorAddress = validator_address, publicKey = pubkey, value = Serializable.Coin(denom = value.denom, amount = value.amount), commission = commission.toCommission, description = description.toDescription, minSelfDelegation = min_self_delegation))
  }

  implicit val createValidatorReads: Reads[CreateValidator] = Json.reads[CreateValidator]

  case class EditValidator(validator_address: String, commission_rate: Option[String], description: Description, min_self_delegation: Option[MicroNumber]) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.EDIT_VALIDATOR, TransactionMessages.EditValidator(validatorAddress = validator_address, commissionRate = commission_rate, description = description.toDescription, minSelfDelegation = min_self_delegation))
  }

  implicit val editValidatorReads: Reads[EditValidator] = Json.reads[EditValidator]

  case class Delegate(delegator_address: String, validator_address: String, amount: Coin) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.DELEGATE, TransactionMessages.Delegate(delegatorAddress = delegator_address, validatorAddress = validator_address, amount = amount.toCoin))
  }

  implicit val delegateReads: Reads[Delegate] = Json.reads[Delegate]

  case class Redelegate(delegator_address: String, validator_src_address: String, validator_dst_address: String, amount: Coin) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.REDELEGATE, TransactionMessages.Redelegate(delegatorAddress = delegator_address, validatorSrcAddress = validator_src_address, validatorDstAddress = validator_dst_address, amount = amount.toCoin))
  }

  implicit val redelegateReads: Reads[Redelegate] = Json.reads[Redelegate]

  case class Undelegate(delegator_address: String, validator_address: String, amount: Coin) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.UNDELEGATE, TransactionMessages.Undelegate(delegatorAddress = delegator_address, validatorAddress = validator_address, amount = amount.toCoin))
  }

  implicit val undelegateReads: Reads[Undelegate] = Json.reads[Undelegate]

  //Asset
  case class AssetMint(from: String, fromID: ID, toID: ID, maintainersID: ID, classificationID: ID, properties: Properties, lock: Height, burn: Height) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ASSET_MINT, TransactionMessages.AssetMint(from = from, fromID = fromID.value.idString, toID = toID.value.idString, maintainersID = maintainersID.value.idString, classificationID = classificationID.value.idString, properties = properties.toProperties, lock = lock.toInt, burn = burn.toInt))
  }

  implicit val assetMintReads: Reads[AssetMint] = Json.reads[AssetMint]

  case class AssetMutate(from: String, assetID: ID, properties: Properties, lock: Height, burn: Height) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ASSET_MUTATE, TransactionMessages.AssetMutate(from = from, assetID = assetID.value.idString, properties = properties.toProperties, lock = lock.toInt, burn = burn.toInt))
  }

  implicit val assetMutateReads: Reads[AssetMutate] = Json.reads[AssetMutate]

  case class AssetBurn(from: String, fromID: ID, assetID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ASSET_BURN, TransactionMessages.AssetBurn(from = from, assetID = assetID.value.idString, fromID = fromID.value.idString))
  }

  implicit val assetBurnReads: Reads[AssetBurn] = Json.reads[AssetBurn]

  //Identity
  case class IdentityIssue(from: String, to: String, fromID: ID, maintainersID: ID, classificationID: ID, properties: Properties) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.IDENTITY_ISSUE, TransactionMessages.IdentityIssue(from = from, to = to, fromID = fromID.value.idString, maintainersID = maintainersID.value.idString, classificationID = classificationID.value.idString, properties = properties.toProperties))
  }

  implicit val identityIssueReads: Reads[IdentityIssue] = Json.reads[IdentityIssue]

  case class IdentityProvision(from: String, to: String, identityID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.IDENTITY_PROVISION, TransactionMessages.IdentityProvision(from = from, to = to, identityID = identityID.value.idString))
  }

  implicit val identityProvisionReads: Reads[IdentityProvision] = Json.reads[IdentityProvision]

  case class IdentityUnprovision(from: String, to: String, identityID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.IDENTITY_UNPROVISION, TransactionMessages.IdentityUnprovision(from = from, to = to, identityID = identityID.value.idString))
  }

  implicit val identityUnprovisionReads: Reads[IdentityUnprovision] = Json.reads[IdentityUnprovision]

  //Split
  case class SplitSend(from: String, fromID: ID, toID: ID, ownableID: ID, split: BigDecimal) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SPLIT_SEND, TransactionMessages.SplitSend(from = from, fromID = fromID.value.idString, toID = toID.value.idString, ownableID = ownableID.value.idString, split = split))
  }

  implicit val splitSendReads: Reads[SplitSend] = Json.reads[SplitSend]

  case class SplitWrap(from: String, fromID: ID, coins: Seq[Coin]) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SPLIT_WRAP, TransactionMessages.SplitWrap(from = from, fromID = fromID.value.idString, coins = coins.map(_.toCoin)))
  }

  implicit val splitWrapReads: Reads[SplitWrap] = Json.reads[SplitWrap]

  case class SplitUnwrap(from: String, fromID: ID, ownableID: ID, split: BigDecimal) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.SPLIT_UNWRAP, TransactionMessages.SplitUnwrap(from = from, fromID = fromID.value.idString, ownableID = ownableID.value.idString, split = split))
  }

  implicit val splitUnwrapReads: Reads[SplitUnwrap] = Json.reads[SplitUnwrap]

  //Order
  case class OrderMake(from: String, maintainersID: ID, makerID: ID, takerID: ID, makerSplit: BigDecimal, makerSplitID: ID, exchangeRate: BigDecimal, takerSplitID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ORDER_MAKE, TransactionMessages.OrderMake(from = from, maintainersID = maintainersID.value.idString, makerID = makerID.value.idString, takerID = takerID.value.idString, makerSplit = makerSplit, makerSplitID = makerSplitID.value.idString, exchangeRate = exchangeRate, takerSplitID = takerSplitID.value.idString))
  }

  implicit val orderMakeReads: Reads[OrderMake] = Json.reads[OrderMake]

  case class OrderTake(from: String, fromID: ID, takerSplit: BigDecimal, orderID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ORDER_TAKE, TransactionMessages.OrderTake(from = from, fromID = fromID.value.idString, takerSplit = takerSplit, orderID = orderID.value.idString))
  }

  implicit val orderTakeReads: Reads[OrderTake] = Json.reads[OrderTake]

  case class OrderCancel(from: String, orderID: ID) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.ORDER_CANCEL, TransactionMessages.OrderCancel(from = from, orderID = orderID.value.idString))
  }

  implicit val orderCancelReads: Reads[OrderCancel] = Json.reads[OrderCancel]

  //Classification
  case class ClassificationDefine(from: String, fromID: ID, maintainersID: ID, traits: Traits) extends TransactionMessageResponse {
    def toStdMsg: StdMsg = StdMsg(constants.TransactionMessage.CLASSIFICATION_DEFINE, TransactionMessages.ClassificationDefine(from = from, fromID = fromID.value.idString, maintainersID = maintainersID.value.idString, traits = traits.traitList.map(_.toTrait)))
  }

  implicit val classificationDefineReads: Reads[ClassificationDefine] = Json.reads[ClassificationDefine]

  def msgApply(msgType: String, value: JsObject): Msg = try {
    msgType match {
      //bank
      case constants.TransactionMessage.SEND_COIN => Msg(msgType, utilities.JSON.convertJsonStringToObject[SendCoin](value.toString))
      //crisis
      case constants.TransactionMessage.VERIFY_INVARIANT => Msg(msgType, utilities.JSON.convertJsonStringToObject[VerifyInvariant](value.toString))
      //distribution
      case constants.TransactionMessage.SET_WITHDRAW_ADDRESS => Msg(msgType, utilities.JSON.convertJsonStringToObject[SetWithdrawAddress](value.toString))
      case constants.TransactionMessage.WITHDRAW_DELEGATOR_REWARD => Msg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawDelegatorReward](value.toString))
      case constants.TransactionMessage.WITHDRAW_VALIDATOR_COMMISSION => Msg(msgType, utilities.JSON.convertJsonStringToObject[WithdrawValidatorCommission](value.toString))
      case constants.TransactionMessage.FUND_COMMUNITY_POOL => Msg(msgType, utilities.JSON.convertJsonStringToObject[FundCommunityPool](value.toString))
      //TODO evidence Pending in cosmos-sdk
      //case constants.TransactionMessage.SUBMIT_EVIDENCE => Msg(msgType, utilities.JSON.convertJsonStringToObject[SubmitEvidence](value.toString))
      //gov
      case constants.TransactionMessage.DEPOSIT => Msg(msgType, utilities.JSON.convertJsonStringToObject[Deposit](value.toString))
      case constants.TransactionMessage.SUBMIT_PROPOSAL => Msg(msgType, utilities.JSON.convertJsonStringToObject[SubmitProposal](value.toString))
      case constants.TransactionMessage.VOTE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Vote](value.toString))
      //slashing
      case constants.TransactionMessage.UNJAIL => Msg(msgType, utilities.JSON.convertJsonStringToObject[Unjail](value.toString))
      //staking
      case constants.TransactionMessage.CREATE_VALIDATOR => Msg(msgType, utilities.JSON.convertJsonStringToObject[CreateValidator](value.toString))
      case constants.TransactionMessage.EDIT_VALIDATOR => Msg(msgType, utilities.JSON.convertJsonStringToObject[EditValidator](value.toString))
      case constants.TransactionMessage.DELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Delegate](value.toString))
      case constants.TransactionMessage.REDELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Redelegate](value.toString))
      case constants.TransactionMessage.UNDELEGATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[Undelegate](value.toString))
      //asset
      case constants.TransactionMessage.ASSET_MINT => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetMint](value.toString))
      case constants.TransactionMessage.ASSET_MUTATE => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetMutate](value.toString))
      case constants.TransactionMessage.ASSET_BURN => Msg(msgType, utilities.JSON.convertJsonStringToObject[AssetBurn](value.toString))
      //identity
      case constants.TransactionMessage.IDENTITY_ISSUE => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityIssue](value.toString))
      case constants.TransactionMessage.IDENTITY_PROVISION => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityProvision](value.toString))
      case constants.TransactionMessage.IDENTITY_UNPROVISION => Msg(msgType, utilities.JSON.convertJsonStringToObject[IdentityUnprovision](value.toString))
      //split
      case constants.TransactionMessage.SPLIT_SEND => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitSend](value.toString))
      case constants.TransactionMessage.SPLIT_WRAP => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitWrap](value.toString))
      case constants.TransactionMessage.SPLIT_UNWRAP => Msg(msgType, utilities.JSON.convertJsonStringToObject[SplitUnwrap](value.toString))
      //order
      case constants.TransactionMessage.ORDER_MAKE => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderMake](value.toString))
      case constants.TransactionMessage.ORDER_TAKE => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderTake](value.toString))
      case constants.TransactionMessage.ORDER_CANCEL => Msg(msgType, utilities.JSON.convertJsonStringToObject[OrderCancel](value.toString))
      //classification
      case constants.TransactionMessage.CLASSIFICATION_DEFINE => Msg(msgType, utilities.JSON.convertJsonStringToObject[ClassificationDefine](value.toString))
      //unknown
      case _ => throw new BaseException(constants.Response.TRANSACTION_TYPE_NOT_FOUND)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.TRANSACTION_STRUCTURE_CHANGED)
  }
}
