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

  case class SoftwareUpgrade(title: String, description: String, plan: Plan) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.SOFTWARE_UPGRADE
  }

  implicit val softwareUpgradeReads: Reads[SoftwareUpgrade] = Json.reads[SoftwareUpgrade]

  implicit val softwareUpgradeWrites: OWrites[SoftwareUpgrade] = Json.writes[SoftwareUpgrade]

  case class Change(subspace: String, key: String, value: String)

  implicit val changeReads: Reads[Change] = Json.reads[Change]

  implicit val changeWrites: OWrites[Change] = Json.writes[Change]

  //TODO
  case class ParameterChange(title: String, description: String, changes: Seq[Change]) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.PARAMETER_CHANGE
  }

  implicit val parameterChangeReads: Reads[ParameterChange] = Json.reads[ParameterChange]

  implicit val parameterChangeWrites: OWrites[ParameterChange] = Json.writes[ParameterChange]

  case class Text(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.TEXT
  }

  implicit val textReads: Reads[Text] = Json.reads[Text]

  implicit val textWrites: OWrites[Text] = Json.writes[Text]

  //TODO onNewBlock updated account balance of recipient if success
  case class CommunityPoolSpend(title: String, description: String, recipient: String, amount: Seq[Coin]) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.COMMUNITY_POOL_SPEND
  }

  implicit val communityPoolSpendReads: Reads[CommunityPoolSpend] = Json.reads[CommunityPoolSpend]

  implicit val communityPoolSpendWrites: OWrites[CommunityPoolSpend] = Json.writes[CommunityPoolSpend]

  case class CancelSoftwareUpgrade(title: String, description: String) extends ProposalContent {
    val proposalContentType: String = constants.Blockchain.Proposal.CANCEL_SOFTWARE_UPGRADE
  }

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

  implicit val proposalContentReads: Reads[ProposalContent] = (
    (JsPath \ "proposalContentType").read[String] and
      JsPath.read[JsObject]
    ) (proposalContentApply _)

  implicit val proposalContentWrites: Writes[ProposalContent] = {
    case cancelSoftwareUpgrade: CancelSoftwareUpgrade => Json.toJson(cancelSoftwareUpgrade)(Json.writes[CancelSoftwareUpgrade])
    case softwareUpgrade: SoftwareUpgrade => Json.toJson(softwareUpgrade)(Json.writes[SoftwareUpgrade])
    case parameterChange: ParameterChange => Json.toJson(parameterChange)(Json.writes[ParameterChange])
    case text: Text => Json.toJson(text)(Json.writes[Text])
    case communityPoolSpend: CommunityPoolSpend => Json.toJson(communityPoolSpend)(Json.writes[CommunityPoolSpend])
    case _ => throw new BaseException(constants.Response.NO_SUCH_PROPOSAL_CONTENT_TYPE)
  }

}
