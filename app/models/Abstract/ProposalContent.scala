package models.Abstract

import com.google.protobuf.{Any => protoAny}
import cosmos.distribution.v1beta1.{Distribution => distributionProto}
import cosmos.gov.v1beta1.{Gov => govProto}
import cosmos.params.v1beta1.{Params => paramsProto}
import cosmos.upgrade.v1beta1.{Upgrade => upgradeProto}
import models.common.ProposalContents._
import models.common.Serializable.Coin
import play.api.Logger

import scala.jdk.CollectionConverters.CollectionHasAsScala


abstract class ProposalContent {
  val title: String
  val description: String

  def toProto: protoAny
}

object ProposalContent {

  private implicit val module: String = constants.Module.PROPOSAL_CONTENT

  private implicit val logger: Logger = Logger(this.getClass)

  def apply(protoProposalContent: protoAny): ProposalContent = protoProposalContent.getTypeUrl match {
    case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => {
      val proposalProto = upgradeProto.CancelSoftwareUpgradeProposal.parseFrom(protoProposalContent.toByteArray)
      CancelSoftwareUpgrade(title = proposalProto.getTitle, description = proposalProto.getDescription)
    }
    case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => {
      val proposalProto = upgradeProto.SoftwareUpgradeProposal.parseFrom(protoProposalContent.toByteArray)
      SoftwareUpgrade(title = proposalProto.getTitle, description = proposalProto.getDescription, plan = Plan(name = proposalProto.getPlan.getName, time = proposalProto.getPlan.getTime.getSeconds, height = proposalProto.getPlan.getHeight.toString, info = proposalProto.getPlan.getInfo))
    }
    case constants.Blockchain.Proposal.PARAMETER_CHANGE => {
      val parameterProto = paramsProto.ParameterChangeProposal.parseFrom(protoProposalContent.getValue)
      ParameterChange(title = parameterProto.getTitle, description = parameterProto.getDescription, changes = parameterProto.getChangesList.asScala.toSeq.map(x => Change.fromProtoAny(x)))
    }
    case constants.Blockchain.Proposal.TEXT => {
      val parameterProto = govProto.TextProposal.parseFrom(protoProposalContent.getValue)
      Text(title = parameterProto.getTitle, description = parameterProto.getDescription)
    }
    case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => {
      val parameterProto = distributionProto.CommunityPoolSpendProposal.parseFrom(protoProposalContent.toByteArray)
      CommunityPoolSpend(title = parameterProto.getTitle, description = parameterProto.getDescription, recipient = parameterProto.getRecipient, amount = parameterProto.getAmountList.asScala.toSeq.map(x => Coin(x)))
    }
  }
}
