package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException

import javax.inject._
import models.{master, memberCheck}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.memberCheck.{GetCorporateScan, GetCorporateScanResult, GetMemberScan, GetMemberScanResult}
import transactions.memberCheck.{CorporateScan, CorporateScanResultDecision, MemberScan, MemberScanResultDecision}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackgroundCheckController @Inject()(
                                           messagesControllerComponents: MessagesControllerComponents,
                                           masterOrganizationUBOs: master.OrganizationUBOs,
                                           memberCheckMemberScans: memberCheck.MemberScans,
                                           memberCheckMemberScanDecisions: memberCheck.MemberScanDecisions,
                                           memberCheckCorporateScans: memberCheck.CorporateScans,
                                           memberCheckCorporateScanDecisions: memberCheck.CorporateScanDecisions,
                                           memberCheckVesselScans: memberCheck.VesselScans,
                                           memberCheckVesselScanDecisions: memberCheck.VesselScanDecisions,
                                           getMemberCheckMemberScan: GetMemberScan,
                                           getMemberCheckMemberScanResult: GetMemberScanResult,
                                           postMemberCheckMemberScan: MemberScan,
                                           postMemberCheckMemberScanResultDecision: MemberScanResultDecision,
                                           getMemberCheckCorporateScan: GetCorporateScan,
                                           getMemberCheckCorporateScanResult: GetCorporateScanResult,
                                           postMemberCheckCorporateScan: CorporateScan,
                                           postMemberCheckCorporateScanResultDecision: CorporateScanResultDecision,
                                           withLoginActionAsync: WithLoginActionAsync,
                                           withUsernameToken: WithUsernameToken,
                                           withoutLoginAction: WithoutLoginAction,
                                           withoutLoginActionAsync: WithoutLoginActionAsync,
                                         )(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BACKGROUND_CHECK

  private implicit val otherApps: Seq[utilities.Configuration.OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    utilities.Configuration.OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  //UBO CHECKS
  def memberScanForm(uboID: String, firstName: String, lastName: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckMemberScan(uboID = uboID, firstName = firstName, lastName = lastName))
  }

  def memberScan: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckMemberScan.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckMemberScan(formWithErrors, formWithErrors.data(constants.FormField.UBO_ID.name), formWithErrors.data(constants.FormField.FIRST_NAME.name), formWithErrors.data(constants.FormField.LAST_NAME.name))))
        },
        memberCheckMemberScanData => {
          val scanID = memberCheckMemberScans.Service.getScanID(memberCheckMemberScanData.firstName, memberCheckMemberScanData.lastName)

          def getResult(scanID: Option[Int]) = {
            def getMemberCheckRequest(scanID: Int) = getMemberCheckMemberScan.Service.get(scanID.toString)

            val memberScanID: Future[Int] = scanID match {
              case Some(id) => Future(id)
              case None =>
                val memberNumber = utilities.IDGenerator.requestID()
                val postMemberCheckRequest = postMemberCheckMemberScan.Service.post(postMemberCheckMemberScan.Request(firstName = memberCheckMemberScanData.firstName, lastName = memberCheckMemberScanData.lastName, memberNumber = memberNumber))

                def createMemberScan(scanID: Int) = memberCheckMemberScans.Service.create(memberNumber, memberCheckMemberScanData.firstName, memberCheckMemberScanData.lastName, scanID)

                for {
                  postResponse <- postMemberCheckRequest
                  _ <- createMemberScan(postResponse.scanId)
                } yield postResponse.scanId
            }

            for {
              memberScanID <- memberScanID
              response <- getMemberCheckRequest(memberScanID)
              result <- withUsernameToken.PartialContent(views.html.component.master.backgroundCheck.memberCheckMemberScanResponse(response, memberCheckMemberScanData.uboID))
            } yield result
          }

          (for {
            scanID <- scanID
            result <- getResult(scanID)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def memberScanResult(resultID: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val result = getMemberCheckMemberScanResult.Service.get(resultID.toString)
      (for {
        result <- result
      } yield Ok(views.html.component.master.backgroundCheck.memberCheckMemberScanResult(result))).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def memberScanResultDecisionForm(resultID: Int): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckMemberScanSingleResultDecision(resultID = resultID))
  }


  def memberScanResultDecision: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckMemberScanResultDecision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckMemberScanSingleResultDecision(formWithErrors, formWithErrors.data(constants.FormField.RESULT_ID.name).toInt)))
        },
        memberCheckMemberScanResultDecisionData => {
          val decision = postMemberCheckMemberScanResultDecision.Service.post(memberCheckMemberScanResultDecisionData.resultID.toString, postMemberCheckMemberScanResultDecision.Request(memberCheckMemberScanResultDecisionData.matchDecision, memberCheckMemberScanResultDecisionData.assessedRisk, memberCheckMemberScanResultDecisionData.comment))
          (for {
            _ <- decision
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def addUBOMemberCheckForm(uboID: String, scanID: Int, resultID: Option[Int]): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.addUBOMemberCheck(uboID = uboID, scanID = scanID, resultID = resultID))
  }

  def addUBOMemberCheck: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.AddUBOMemberCheck.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.addUBOMemberCheck(formWithErrors, formWithErrors.data(constants.FormField.ORGANIZATION_ID.name), formWithErrors.data(constants.FormField.SCAN_ID.name).toInt, Option(formWithErrors.data(constants.FormField.RESULT_ID.name).toInt))))
        },
        addUBOBackgroundCheckData => {
          val memberScan = memberCheckMemberScans.Service.tryGetByScanID(addUBOBackgroundCheckData.scanID)

          def createMemberCheckUBODecision = memberCheckMemberScanDecisions.Service.insertOrUpdate(addUBOBackgroundCheckData.uboID, addUBOBackgroundCheckData.scanID, addUBOBackgroundCheckData.resultID, addUBOBackgroundCheckData.status)

          def updateUBOStatus = masterOrganizationUBOs.Service.markVerified(addUBOBackgroundCheckData.uboID, addUBOBackgroundCheckData.status)

          (for {
            memberScan <- memberScan
            _ <- createMemberCheckUBODecision
            _ <- updateUBOStatus
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  //CORPORATE SCAN
  def corporateScanForm(organizationID: String, companyName: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckCorporateScan(organizationID = organizationID, companyName = companyName))
  }

  def corporateScan: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckCorporateScan.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckCorporateScan(formWithErrors, formWithErrors.data(constants.FormField.ORGANIZATION_ID.name), formWithErrors.data(constants.FormField.COMPANY_NAME.name))))
        },
        memberCheckCorporateScanData => {
          val scanID = memberCheckCorporateScans.Service.getScanID(memberCheckCorporateScanData.companyName)

          def getResult(scanID: Option[Int]) = {
            def getMemberCheckRequest(scanID: Int) = getMemberCheckCorporateScan.Service.get(scanID.toString)

            val corporateScanID: Future[Int] = scanID match {
              case Some(id) => Future(id)
              case None =>
                val entityNumber = utilities.IDGenerator.requestID()
                val postMemberCheckRequest = postMemberCheckCorporateScan.Service.post(postMemberCheckCorporateScan.Request(companyName = memberCheckCorporateScanData.companyName, entityNumber = entityNumber))

                def createCorporateScan(scanID: Int) = memberCheckCorporateScans.Service.create(entityNumber, memberCheckCorporateScanData.companyName, scanID)

                for {
                  postResponse <- postMemberCheckRequest
                  _ <- createCorporateScan(postResponse.scanId)
                } yield postResponse.scanId
            }

            for {
              corporateScanID <- corporateScanID
              response <- getMemberCheckRequest(corporateScanID)
              result <- withUsernameToken.PartialContent(views.html.component.master.backgroundCheck.memberCheckCorporateScanResponse(response, memberCheckCorporateScanData.organizationID))
            } yield result
          }

          (for {
            scanID <- scanID
            result <- getResult(scanID)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def corporateScanResult(resultID: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val result = getMemberCheckCorporateScanResult.Service.get(resultID.toString)
      (for {
        result <- result
      } yield Ok(views.html.component.master.backgroundCheck.memberCheckCorporateScanResult(result))).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def corporateScanResultDecisionForm(resultID: Int): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckCorporateScanSingleResultDecision(resultID = resultID))
  }


  def corporateScanResultDecision: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckCorporateScanResultDecision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckCorporateScanSingleResultDecision(formWithErrors, formWithErrors.data(constants.FormField.RESULT_ID.name).toInt)))
        },
        memberCheckCorporateScanResultDecisionData => {
          val decision = postMemberCheckCorporateScanResultDecision.Service.post(memberCheckCorporateScanResultDecisionData.resultID.toString, postMemberCheckCorporateScanResultDecision.Request(memberCheckCorporateScanResultDecisionData.matchDecision, memberCheckCorporateScanResultDecisionData.assessedRisk, memberCheckCorporateScanResultDecisionData.comment))
          (for {
            _ <- decision
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def addOrganizationMemberCheckForm(organizationID: String, scanID: Int, resultID: Option[Int]): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.addOrganizationMemberCheck(organizationID = organizationID, scanID = scanID, resultID = resultID))
  }


  def addOrganizationMemberCheck: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganizationMemberCheck.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.addOrganizationMemberCheck(formWithErrors, formWithErrors.data(constants.FormField.ORGANIZATION_ID.name), formWithErrors.data(constants.FormField.SCAN_ID.name).toInt, Option(formWithErrors.data(constants.FormField.RESULT_ID.name).toInt))))
        },
        addOrganizationBackgroundCheckData => {
          val corporateScan = memberCheckCorporateScans.Service.tryGetByScanID(addOrganizationBackgroundCheckData.scanID)

          def createMemberCheckOrganizationDecision = memberCheckCorporateScanDecisions.Service.insertOrUpdate(addOrganizationBackgroundCheckData.organizationID, addOrganizationBackgroundCheckData.scanID, addOrganizationBackgroundCheckData.resultID, addOrganizationBackgroundCheckData.status)

          (for {
            _ <- corporateScan
            _ <- createMemberCheckOrganizationDecision
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  //VESSEL SCAN
  def vesselScanForm(assetID: String, vesselName: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckVesselScan(assetID = assetID, vesselName = vesselName))
  }

  def vesselScan: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckVesselScan.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckVesselScan(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name), formWithErrors.data(constants.FormField.VESSEL_NAME.name))))
        },
        memberCheckVesselScanData => {
          val scanID = memberCheckVesselScans.Service.getScanID(memberCheckVesselScanData.vesselName)

          def getResult(scanID: Option[Int]) = {
            def getMemberCheckRequest(scanID: Int) = getMemberCheckCorporateScan.Service.get(scanID.toString)

            val vesselScanID: Future[Int] = scanID match {
              case Some(id) => Future(id)
              case None =>
                val entityNumber = utilities.IDGenerator.requestID()
                val postMemberCheckRequest = postMemberCheckCorporateScan.Service.post(postMemberCheckCorporateScan.Request(companyName = memberCheckVesselScanData.vesselName, entityNumber = entityNumber))

                def createVesselScan(scanID: Int) = memberCheckVesselScans.Service.create(entityNumber, memberCheckVesselScanData.vesselName, scanID)

                for {
                  postResponse <- postMemberCheckRequest
                  _ <- createVesselScan(postResponse.scanId)
                } yield postResponse.scanId
            }

            for {
              vesselScanID <- vesselScanID
              response <- getMemberCheckRequest(vesselScanID)
              result <- withUsernameToken.PartialContent(views.html.component.master.backgroundCheck.memberCheckVesselScanResponse(response, memberCheckVesselScanData.assetID))
            } yield result
          }

          (for {
            scanID <- scanID
            result <- getResult(scanID)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def vesselScanResult(resultID: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val result = getMemberCheckCorporateScanResult.Service.get(resultID.toString)
      (for {
        result <- result
      } yield Ok(views.html.component.master.backgroundCheck.memberCheckVesselScanResult(result))).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def vesselScanResultDecisionForm(resultID: Int): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.memberCheckVesselScanSingleResultDecision(resultID = resultID))
  }

  def vesselScanResultDecision: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.MemberCheckVesselScanResultDecision.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.memberCheckVesselScanSingleResultDecision(formWithErrors, formWithErrors.data(constants.FormField.RESULT_ID.name).toInt)))
        },
        memberCheckVesselScanResultDecisionData => {
          val decision = postMemberCheckCorporateScanResultDecision.Service.post(memberCheckVesselScanResultDecisionData.resultID.toString, postMemberCheckCorporateScanResultDecision.Request(memberCheckVesselScanResultDecisionData.matchDecision, memberCheckVesselScanResultDecisionData.assessedRisk, memberCheckVesselScanResultDecisionData.comment))
          (for {
            _ <- decision
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }

  def addAssetMemberCheckForm(assetID: String, scanID: Int, resultID: Option[Int]): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.backgroundCheck.addAssetMemberCheck(assetID = assetID, scanID = scanID, resultID = resultID))
  }


  def addAssetMemberCheck: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.AddAssetMemberCheck.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.backgroundCheck.addAssetMemberCheck(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name), formWithErrors.data(constants.FormField.SCAN_ID.name).toInt, Option(formWithErrors.data(constants.FormField.RESULT_ID.name).toInt))))
        },
        addAssetBackgroundCheckData => {
          val corporateScan = memberCheckVesselScans.Service.tryGetByScanID(addAssetBackgroundCheckData.scanID)

          def createMemberCheckVesselDecision = memberCheckVesselScanDecisions.Service.insertOrUpdate(addAssetBackgroundCheckData.assetID, addAssetBackgroundCheckData.scanID, addAssetBackgroundCheckData.resultID, addAssetBackgroundCheckData.status)

          (for {
            _ <- corporateScan
            _ <- createMemberCheckVesselDecision
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.DECISION_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }
}
