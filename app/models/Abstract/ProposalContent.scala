package models.Abstract

import com.cosmos.distribution.{v1beta1 => distributionProto}
import com.cosmos.gov.{v1beta1 => govProto}
import com.cosmos.params.{v1beta1 => paramsProto}
import com.cosmos.upgrade.{v1beta1 => upgradeProto}
import com.google.protobuf.{Any => protoAny}
import com.ibc.core.client.v1.ClientUpdateProposal
import models.common.ProposalContents._
import models.common.Serializable.Coin
import play.api.Logger

import scala.jdk.CollectionConverters.CollectionHasAsScala

abstract class ProposalContent {
  val title: String
  val description: String

  def getType: String

  def toProto: protoAny
}

object ProposalContent {

  private implicit val module: String = constants.Module.PROPOSAL_CONTENT

  private implicit val logger: Logger = Logger(this.getClass)

  def apply(protoProposalContent: protoAny): ProposalContent = protoProposalContent.getTypeUrl match {
    case schema.constants.Proposal.CANCEL_SOFTWARE_UPGRADE => {
      val proposalProto = upgradeProto.CancelSoftwareUpgradeProposal.parseFrom(protoProposalContent.getValue)
      CancelSoftwareUpgrade(title = proposalProto.getTitle, description = proposalProto.getDescription)
    }
    case schema.constants.Proposal.SOFTWARE_UPGRADE => {
      val proposalProto = upgradeProto.SoftwareUpgradeProposal.parseFrom(protoProposalContent.getValue)
      SoftwareUpgrade(title = proposalProto.getTitle, description = proposalProto.getDescription, plan = Plan(name = proposalProto.getPlan.getName, time = proposalProto.getPlan.getTime.getSeconds, height = proposalProto.getPlan.getHeight.toString, info = proposalProto.getPlan.getInfo))
    }
    case schema.constants.Proposal.PARAMETER_CHANGE => {
      val parameterProto = paramsProto.ParameterChangeProposal.parseFrom(protoProposalContent.getValue)
      ParameterChange(title = parameterProto.getTitle, description = parameterProto.getDescription, changes = parameterProto.getChangesList.asScala.toSeq.map(x => Change.fromProtoAny(x)))
    }
    case schema.constants.Proposal.TEXT => {
      val parameterProto = govProto.TextProposal.parseFrom(protoProposalContent.getValue)
      Text(title = parameterProto.getTitle, description = parameterProto.getDescription)
    }
    case schema.constants.Proposal.COMMUNITY_POOL_SPEND => {
      val parameterProto = distributionProto.CommunityPoolSpendProposal.parseFrom(protoProposalContent.getValue)
      CommunityPoolSpend(title = parameterProto.getTitle, description = parameterProto.getDescription, recipient = parameterProto.getRecipient, amount = parameterProto.getAmountList.asScala.toSeq.map(x => Coin(x)))
    }
    case schema.constants.Proposal.IBC_CLIENT_UPDATE => {
      val parameterProto = ClientUpdateProposal.parseFrom(protoProposalContent.getValue)
      IBCClientUpdate(title = parameterProto.getTitle, description = parameterProto.getDescription, subjectClientId = parameterProto.getSubjectClientId, substituteClientId = parameterProto.getSubstituteClientId)
    }
  }
}
