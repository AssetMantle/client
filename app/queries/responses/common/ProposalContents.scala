package queries.responses.common

import models.common.{ProposalContents => SerializableProposalContents}
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.Abstract.ProposalContent
import utilities.Date.RFC3339

object ProposalContents {
  private implicit val module: String = constants.Module.PROPOSAL_CONTENT_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Plan(name: String, time: RFC3339, height: String, info: String) {
    def toSerializablePlan: SerializableProposalContents.Plan = SerializableProposalContents.Plan(name = name, time = time.epoch, height = height, info = info)
  }

  implicit val plainReads: Reads[Plan] = Json.reads[Plan]

  case class SoftwareUpgrade(title: String, description: String, plan: Plan) extends ProposalContent {
    val proposalContentType: String = schema.constants.Proposal.SOFTWARE_UPGRADE

    def toSerializableProposalContent: SerializableProposalContents.SoftwareUpgrade = SerializableProposalContents.SoftwareUpgrade(title = title, description = description, plan = plan.toSerializablePlan)
  }

  implicit val softwareUpgradeReads: Reads[SoftwareUpgrade] = Json.reads[SoftwareUpgrade]

  case class Change(subspace: String, key: String, value: String) {
    def toSerializableChange: SerializableProposalContents.Change = SerializableProposalContents.Change(subspace = subspace, key = key, value = value)
  }

  implicit val changeReads: Reads[Change] = Json.reads[Change]

  case class ParameterChange(title: String, description: String, changes: Seq[Change]) extends ProposalContent {
    val proposalContentType: String = schema.constants.Proposal.PARAMETER_CHANGE

    def toSerializableProposalContent: SerializableProposalContents.ParameterChange = SerializableProposalContents.ParameterChange(title = title, description = description, changes = changes.map(_.toSerializableChange))
  }

  implicit val parameterChangeReads: Reads[ParameterChange] = Json.reads[ParameterChange]

  case class Text(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = schema.constants.Proposal.TEXT

    def toSerializableProposalContent: SerializableProposalContents.Text = SerializableProposalContents.Text(title = title, description = description)
  }

  implicit val textReads: Reads[Text] = Json.reads[Text]

  case class CommunityPoolSpend(title: String, description: String, recipient: String, amount: Seq[Coin]) extends ProposalContent {
    val proposalContentType: String = schema.constants.Proposal.COMMUNITY_POOL_SPEND

    def toSerializableProposalContent: SerializableProposalContents.CommunityPoolSpend = SerializableProposalContents.CommunityPoolSpend(title = title, description = description, recipient = recipient, amount = amount.map(_.toCoin))
  }

  implicit val communityPoolSpendReads: Reads[CommunityPoolSpend] = Json.reads[CommunityPoolSpend]

  case class CancelSoftwareUpgrade(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = schema.constants.Proposal.CANCEL_SOFTWARE_UPGRADE

    def toSerializableProposalContent: SerializableProposalContents.CancelSoftwareUpgrade = SerializableProposalContents.CancelSoftwareUpgrade(title = title, description = description)
  }

  implicit val cancelSoftwareUpgradeReads: Reads[CancelSoftwareUpgrade] = Json.reads[CancelSoftwareUpgrade]

  def proposalContentApply(proposalContentType: String, value: JsObject): ProposalContent = proposalContentType match {
    case schema.constants.Proposal.CANCEL_SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[CancelSoftwareUpgrade](value.toString)
    case schema.constants.Proposal.SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[SoftwareUpgrade](value.toString)
    case schema.constants.Proposal.PARAMETER_CHANGE => utilities.JSON.convertJsonStringToObject[ParameterChange](value.toString)
    case schema.constants.Proposal.TEXT => utilities.JSON.convertJsonStringToObject[Text](value.toString)
    case schema.constants.Proposal.COMMUNITY_POOL_SPEND => utilities.JSON.convertJsonStringToObject[CommunityPoolSpend](value.toString)
    case _ => constants.Response.NO_SUCH_PROPOSAL_CONTENT_TYPE.throwBaseException()
  }

}
