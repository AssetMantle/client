package models.common

import com.cosmos.distribution.{v1beta1 => distributionProto}
import com.cosmos.gov.{v1beta1 => govProto}
import com.cosmos.params.{v1beta1 => paramsProto}
import com.cosmos.upgrade.{v1beta1 => upgradeProto}
import com.google.protobuf.{Any => protoAny, Timestamp => protoTimestamp}
import models.Abstract.ProposalContent
import models.common.Serializable.Coin

import scala.jdk.CollectionConverters.IterableHasAsJava

object ProposalContents {

  case class Plan(name: String, time: Long, height: String, info: String)

  case class SoftwareUpgrade(title: String, description: String, plan: Plan) extends ProposalContent {

    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Proposal.SOFTWARE_UPGRADE)
      .setValue(
        upgradeProto.SoftwareUpgradeProposal.newBuilder()
          .setTitle(this.title)
          .setDescription(this.description)
          .setPlan(upgradeProto.Plan.newBuilder()
            .setName(this.plan.name)
            .setHeight(this.plan.height.toLong)
            .setInfo(this.plan.info)
            .setTime(protoTimestamp.newBuilder().setSeconds(this.plan.time))
            .build()
          )
          .build().toByteString
      )
      .build()
  }

  case class Change(subspace: String, key: String, value: String)

  object Change {
    def fromProtoAny(parameterChangeProto: paramsProto.ParamChange): Change = Change(subspace = parameterChangeProto.getSubspace, key = parameterChangeProto.getKey, value = parameterChangeProto.getValue)
  }

  case class ParameterChange(title: String, description: String, changes: Seq[Change]) extends ProposalContent {
    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Proposal.PARAMETER_CHANGE)
      .setValue(
        paramsProto.ParameterChangeProposal.newBuilder()
          .setTitle(this.title)
          .setDescription(this.description)
          .addAllChanges(this.changes.map(x => paramsProto.ParamChange.newBuilder().setKey(x.key).setValue(x.value).setSubspace(x.subspace).build()).asJava)
          .build().toByteString
      )
      .build()
  }

  case class Text(title: String, description: String) extends ProposalContent {
    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Proposal.TEXT)
      .setValue(
        govProto.TextProposal.newBuilder()
          .setTitle(this.title)
          .setDescription(this.description)
          .build().toByteString
      )
      .build()
  }

  case class CommunityPoolSpend(title: String, description: String, recipient: String, amount: Seq[Coin]) extends ProposalContent {
    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND)
      .setValue(
        distributionProto.CommunityPoolSpendProposal.newBuilder()
          .setTitle(this.title)
          .setDescription(this.description)
          .setRecipient(this.recipient)
          .addAllAmount(this.amount.map(_.toProtoCoin).asJava)
          .build().toByteString
      )
      .build()
  }

  case class CancelSoftwareUpgrade(title: String, description: String) extends ProposalContent {
    def toProto: protoAny = protoAny.newBuilder()
      .setTypeUrl(constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE)
      .setValue(
        upgradeProto.CancelSoftwareUpgradeProposal.newBuilder()
          .setTitle(this.title)
          .setDescription(this.description)
          .build().toByteString
      )
      .build()
  }
}
