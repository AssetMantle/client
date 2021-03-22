package queries.responses.common

import exceptions.BaseException
import models.common.{ProposalContents => SerializableProposalContents}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, OWrites, Reads}
import queries.Abstract.ProposalContent

object ProposalContents {
  private implicit val module: String = constants.Module.PROPOSAL_CONTENT_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Plan(name: String, time: String, height: String, info: String) {
    def toSerializablePlan: SerializableProposalContents.Plan = SerializableProposalContents.Plan(name = name, time = time, height = height, info = info)
  }

  implicit val plainReads: Reads[Plan] = Json.reads[Plan]

  case class SoftwareUpgrade(title: String, description: String, plan: Plan) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.SOFTWARE_UPGRADE

    def toSerializableProposalContent: SerializableProposalContents.SoftwareUpgrade = SerializableProposalContents.SoftwareUpgrade(title = title, description = description, plan = plan.toSerializablePlan)
  }

  implicit val softwareUpgradeReads: Reads[SoftwareUpgrade] = Json.reads[SoftwareUpgrade]

  case class Change(subspace: String, key: String, value: String) {
    def toSerializableChange: SerializableProposalContents.Change = SerializableProposalContents.Change(subspace = subspace, key = key, value = value)
  }

  implicit val changeReads: Reads[Change] = Json.reads[Change]

  case class ParameterChange(title: String, description: String, changes: Seq[Change]) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.PARAMETER_CHANGE

    def toSerializableProposalContent: SerializableProposalContents.ParameterChange = SerializableProposalContents.ParameterChange(title = title, description = description, changes = changes.map(_.toSerializableChange))
  }

  implicit val parameterChangeReads: Reads[ParameterChange] = Json.reads[ParameterChange]

  case class Text(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.TEXT

    def toSerializableProposalContent: SerializableProposalContents.Text = SerializableProposalContents.Text(title = title, description = description)
  }

  implicit val textReads: Reads[Text] = Json.reads[Text]

  case class CommunityPoolSpend(title: String, description: String, recipient: String, amount: Seq[Coin]) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND

    def toSerializableProposalContent: SerializableProposalContents.CommunityPoolSpend = SerializableProposalContents.CommunityPoolSpend(title = title, description = description, recipient = recipient, amount = amount.map(_.toCoin))
  }

  implicit val communityPoolSpendReads: Reads[CommunityPoolSpend] = Json.reads[CommunityPoolSpend]

  case class CancelSoftwareUpgrade(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE

    def toSerializableProposalContent: SerializableProposalContents.CancelSoftwareUpgrade = SerializableProposalContents.CancelSoftwareUpgrade(title = title, description = description)
  }

  implicit val cancelSoftwareUpgradeReads: Reads[CancelSoftwareUpgrade] = Json.reads[CancelSoftwareUpgrade]

  def proposalContentApply(proposalContentType: String, value: JsObject): ProposalContent = proposalContentType match {
    case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[CancelSoftwareUpgrade](value.toString)
    case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[SoftwareUpgrade](value.toString)
    case constants.Blockchain.Proposal.PARAMETER_CHANGE => utilities.JSON.convertJsonStringToObject[ParameterChange](value.toString)
    case constants.Blockchain.Proposal.TEXT => utilities.JSON.convertJsonStringToObject[Text](value.toString)
    case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => utilities.JSON.convertJsonStringToObject[CommunityPoolSpend](value.toString)
    case _ => throw new BaseException(constants.Response.NO_SUCH_PROPOSAL_CONTENT_TYPE)
  }

  // Cannot do simply as interface with member `@type` because structure of TextProposal and CancelSoftwareUpgrade is exactly same.
  // Though TextProposal seems to be part of legacy
  implicit val proposalContentReads: Reads[ProposalContent] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (proposalContentApply _)

}
