package controllers

import controllers.actions._
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models._
import models.master._
import models.masterTransaction.{SendFiatRequest, _}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         messagesControllerComponents: MessagesControllerComponents,
                                         blockchainFiats: blockchain.Fiats,
                                         masterAssets: master.Assets,
                                         masterAssetHistories: master.AssetHistories,
                                         masterAccountFiles: master.AccountFiles,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterEmails: master.Emails,
                                         masterIdentifications: master.Identifications,
                                         masterMobiles: master.Mobiles,
                                         masterNegotiations: master.Negotiations,
                                         masterNegotiationHistories: master.NegotiationHistories,
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterOrganizations: master.Organizations,
                                         masterOrganizationUBOs: master.OrganizationUBOs,
                                         masterTraders: master.Traders,
                                         masterTraderRelations: master.TraderRelations,
                                         masterZones: master.Zones,
                                         masterFiats: master.Fiats,
                                         masterOrders: master.Orders,
                                         masterOrderHistories: master.OrderHistories,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         masterTransactionAssetFileHistories: masterTransaction.AssetFileHistories,
                                         docusignEnvelopes: docusign.Envelopes,
                                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                         masterTransactionNegotiationFileHistories: masterTransaction.NegotiationFileHistories,
                                         masterTransactionReceiveFiats: masterTransaction.ReceiveFiats,
                                         masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                         masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                         masterTransactionSendFiatRequestHistories: masterTransaction.SendFiatRequestHistories,
                                         memberCheckVesselScanDecisions: memberCheck.VesselScanDecisions,
                                         masterTransactionReceiveFiatHistories: masterTransaction.ReceiveFiatHistories,
                                         westernUnionFiatRequests: westernUnion.FiatRequests,
                                         westernUnionRTCBs: westernUnion.RTCBs,
                                         withLoginAction: WithLoginAction,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
                                         withZoneLoginAction: WithZoneLoginAction,
                                         withoutLoginAction: WithoutLoginAction,
                                         withoutLoginActionAsync: WithoutLoginActionAsync,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

  private val keepAliveDuration = configuration.get[Int]("comet.keepAliveDuration").seconds

  def comet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(actors.Service.Comet.createSource(loginState.username, keepAliveDuration) via Comet.json("parent.cometMessage")).as(ContentTypes.HTML))
  }

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.commonHome()))
  }

  def recentActivities: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.recentActivities()))
  }

  def profilePicture(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield Ok(views.html.profilePicture(profilePicture))
        ).recover {
        case _: BaseException => InternalServerError(views.html.profilePicture())
      }
  }

  def identification: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.AccountKYC.IDENTIFICATION)
      val identification = masterIdentifications.Service.get(loginState.username)
      for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identification(identification = identification, accountKYC = accountKYC))
  }

}