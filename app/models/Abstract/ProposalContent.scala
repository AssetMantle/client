package models.Abstract

import exceptions.BaseException
import models.common.ProposalContents.{CancelSoftwareUpgrade, CommunityPoolSpend, ParameterChange, SoftwareUpgrade, Text, proposalContentApply}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

abstract class ProposalContent {
  val proposalContentType: String
  val title: String
  val description: String
}

object ProposalContent {

  private implicit val module: String = constants.Module.PROPOSAL_CONTENT

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val proposalContentReads: Reads[ProposalContent] = (
    (JsPath \ "proposalContentType").read[String] and
      JsPath.read[JsObject]
    ) (proposalContentApply _)

  implicit val proposalContentWrites: Writes[ProposalContent] = {
    case cancelSoftwareUpgrade: CancelSoftwareUpgrade => Json.toJson(cancelSoftwareUpgrade)
    case softwareUpgrade: SoftwareUpgrade => Json.toJson(softwareUpgrade)
    case parameterChange: ParameterChange => Json.toJson(parameterChange)
    case text: Text => Json.toJson(text)
    case communityPoolSpend: CommunityPoolSpend => Json.toJson(communityPoolSpend)
    case _ => throw new BaseException(constants.Response.NO_SUCH_PROPOSAL_CONTENT_TYPE)
  }
}
