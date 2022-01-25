package models.common

import exceptions.BaseException
import models.Abstract.ProposalContent
import models.common.Serializable.Coin
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, OWrites, Reads, Writes}

object ProposalContents {

  private implicit val module: String = constants.Module.PROPOSAL_CONTENT

  private implicit val logger: Logger = Logger(this.getClass)

  case class Plan(name: String, time: String, height: String, info: String)

  implicit val plainReads: Reads[Plan] = Json.reads[Plan]

  implicit val plainWrites: OWrites[Plan] = Json.writes[Plan]

  case class SoftwareUpgrade(title: String, description: String, plan: Plan, proposalContentType: String = constants.Blockchain.Proposal.SOFTWARE_UPGRADE) extends ProposalContent

  implicit val softwareUpgradeReads: Reads[SoftwareUpgrade] = Json.reads[SoftwareUpgrade]

  implicit val softwareUpgradeWrites: OWrites[SoftwareUpgrade] = Json.writes[SoftwareUpgrade]

  case class Change(subspace: String, key: String, value: String)

  implicit val changeReads: Reads[Change] = Json.reads[Change]

  implicit val changeWrites: OWrites[Change] = Json.writes[Change]

  case class ParameterChange(title: String, description: String, changes: Seq[Change], proposalContentType: String = constants.Blockchain.Proposal.PARAMETER_CHANGE) extends ProposalContent

  implicit val parameterChangeReads: Reads[ParameterChange] = Json.reads[ParameterChange]

  implicit val parameterChangeWrites: OWrites[ParameterChange] = Json.writes[ParameterChange]

  case class Text(title: String, description: String, proposalContentType: String = constants.Blockchain.Proposal.TEXT) extends ProposalContent

  implicit val textReads: Reads[Text] = Json.reads[Text]

  implicit val textWrites: OWrites[Text] = Json.writes[Text]

  case class CommunityPoolSpend(title: String, description: String, recipient: String, amount: Seq[Coin], proposalContentType: String = constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND) extends ProposalContent

  implicit val communityPoolSpendReads: Reads[CommunityPoolSpend] = Json.reads[CommunityPoolSpend]

  implicit val communityPoolSpendWrites: OWrites[CommunityPoolSpend] = Json.writes[CommunityPoolSpend]

  case class CancelSoftwareUpgrade(title: String, description: String, proposalContentType: String = constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE) extends ProposalContent

  implicit val cancelSoftwareUpgradeReads: Reads[CancelSoftwareUpgrade] = Json.reads[CancelSoftwareUpgrade]

  implicit val cancelSoftwareUpgradeWrites: OWrites[CancelSoftwareUpgrade] = Json.writes[CancelSoftwareUpgrade]

  def proposalContentApply(proposalContentType: String, value: JsObject): ProposalContent = proposalContentType match {
    case constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[CancelSoftwareUpgrade](value.toString)
    case constants.Blockchain.Proposal.SOFTWARE_UPGRADE => utilities.JSON.convertJsonStringToObject[SoftwareUpgrade](value.toString)
    case constants.Blockchain.Proposal.PARAMETER_CHANGE => utilities.JSON.convertJsonStringToObject[ParameterChange](value.toString)
    case constants.Blockchain.Proposal.TEXT => utilities.JSON.convertJsonStringToObject[Text](value.toString)
    case constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND => utilities.JSON.convertJsonStringToObject[CommunityPoolSpend](value.toString)
    case _ => throw new BaseException(constants.Response.NO_SUCH_PROPOSAL_CONTENT_TYPE)
  }

}
