package controllers

import controllers.actions._
import exceptions.BaseException
import models._
import models.common.Serializable._
import models.kycCheck.worldcheck.WorldCheckKYCs
import models.master._
import models.masterTransaction.{SendFiatRequest, _}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html
import utilities.MicroNumber
import utilities.MicroNumber._

import java.text.DecimalFormat
import java.time.Year
import java.util.Calendar
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         messagesControllerComponents: MessagesControllerComponents,
                                         blockchainFiats: blockchain.Fiats,
                                         blockchainAccounts: blockchain.Accounts,
                                         blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids,
                                         blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids,
                                         blockchainTransactionSendAssets: blockchainTransaction.SendAssets,
                                         blockchainTransactionSendFiats: blockchainTransaction.SendFiats,
                                         blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders,
                                         blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                         blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets,
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
                                         worldCheckKycs: WorldCheckKYCs
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

  def fiatList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(loginState.address)
      (for {
        fiatPegWallet <- fiatPegWallet
      } yield Ok(views.html.component.master.fiatList(fiatPegWallet))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderFinancials: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderID(traderID)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderID: String, fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]): MicroNumber = {
        getIncompleteNegotiationList(negotiations.filter(_.buyerTraderID == traderID), completedOrders).map(_.price).sum - fiatsInOrders.filter(_.traderID == traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderID: String): MicroNumber = incompleteNegotiations.filter(_.sellerTraderID == traderID).map(_.price).sum

      (for {
        traderID <- traderID
        fiatPegWallet <- getFiatPegWallet(traderID)
        negotiationList <- getNegotiationList(traderID)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.negotiationID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.traderFinancials(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderID, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderID)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationFinancial: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def getNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        getIncompleteNegotiationList(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum

      (for {
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
        negotiationList <- getNegotiationList(traderIDList)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.negotiationID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.organizationFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderIDList, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderIDList)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneFinancial: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderIDList(zoneID: String) = masterTraders.Service.getVerifiedTraderIDsByZoneID(zoneID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def getNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def getIncompleteNegotiationList(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def getCompletedOrderList(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def getFiatsInOrderList(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def getPayable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        getIncompleteNegotiationList(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def getReceivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum

      (for {
        zoneID <- zoneID
        traderIDList <- getTraderIDList(zoneID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
        negotiationList <- getNegotiationList(traderIDList)
        completedOrderList <- getCompletedOrderList(negotiationList.map(_.negotiationID.getOrElse("")))
        fiatsInOrderList <- getFiatsInOrderList(getIncompleteNegotiationList(negotiationList, completedOrderList).map(_.id))
      } yield Ok(views.html.component.master.zoneFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = getPayable(traderIDList, fiatsInOrderList, completedOrderList, negotiationList), receivable = getReceivable(getIncompleteNegotiationList(negotiationList, completedOrderList), traderIDList)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.traderViewAcceptedNegotiationList())
  }

  def traderViewAcceptedBuyNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getBuyNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        buyNegotiationList <- getBuyNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedSellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSellNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        sellNegotiationList <- getSellNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getCompletedNegotiationList(traderID: String): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssetHistories.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        completedNegotiationList <- getCompletedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (negotiation.sellerTraderID == traderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewCompletedNegotiationList(traderID = traderID, completedNegotiationList = completedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.traderViewSentReceivedIncompleteRejectedFailedNegotiationList())
  }

  def traderViewSentNegotiationRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSentNegotiationRequestList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        sentNegotiationRequestList <- getSentNegotiationRequestList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewReceivedNegotiationRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getReceivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        receivedNegotiationList <- getReceivedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewReceivedNegotiationRequestList(receivedNegotiationRequestList = receivedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewIncompleteNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getIncompleteNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        incompleteNegotiationList <- getIncompleteNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewRejectedAndFailedNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getRejectedReceivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderID(traderID)

      def getRejectedSentNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderID(traderID)

      def getFailedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderID(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        rejectedReceivedNegotiationList <- getRejectedReceivedNegotiationList(traderID)
        rejectedSentNegotiationList <- getRejectedSentNegotiationList(traderID)
        failedNegotiationList <- getFailedNegotiationList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.traderViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.organizationViewAcceptedNegotiationList())
  }

  def organizationViewAcceptedBuyNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getBuyNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        buyNegotiationList <- getBuyNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedSellNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSellNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        sellNegotiationList <- getSellNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getCompletedNegotiationList(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssetHistories.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        completedNegotiationList <- getCompletedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (traderList.map(_.id) contains negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewCompletedNegotiationList(completedNegotiationList = completedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.organizationViewSentReceivedIncompleteRejectedFailedNegotiationList())
  }

  def organizationViewSentNegotiationRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSentNegotiationRequestList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        sentNegotiationRequestList <- getSentNegotiationRequestList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewReceivedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        receivedNegotiationList <- getReceivedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewReceivedNegotiationList(receivedNegotiationList = receivedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewIncompleteNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getIncompleteNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        incompleteNegotiationList <- getIncompleteNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedAndFailedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDList(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getRejectedReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs)

      def getRejectedSentNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderIDs(traderIDs)

      def getFailedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderIDs(traderIDs)

      def getTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDList <- getOrganizationTradersIDList(organizationID)
        rejectedReceivedNegotiationList <- getRejectedReceivedNegotiationList(organizationTradersIDList)
        rejectedSentNegotiationList <- getRejectedSentNegotiationList(organizationTradersIDList)
        failedNegotiationList <- getFailedNegotiationList(organizationTradersIDList)
        traderList <- getTraderList(organizationTradersIDList)
        counterPartyTraderList <- getCounterPartyTraderList(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizationList <- getCounterPartyOrganizationList(counterPartyTraderList.map(_.organizationID))
        assetList <- getAssetList((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, traderList = traderList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList, assetList = assetList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def recentActivities: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.recentActivities()))
  }

  def tradeActivities(negotiationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeActivities(negotiationID)))
  }

  def completedTradeActivities(negotiationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.completedTradeActivities(negotiationID)))
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

  def organizationViewTraderAccountList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTraderAccountList()))
  }

  def organizationViewAcceptedTraderAccountList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)

      def acceptedTraders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      (for {
        organizationID <- organizationID
        acceptedTraders <- acceptedTraders(organizationID)
      } yield Ok(views.html.component.master.organizationViewAcceptedTraderAccountList(acceptedTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedTraderAccount(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewAcceptedTraderAccount(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewPendingTraderRequestList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def pendingTraderRequests(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationPendingTraderRequestList(organizationID)

      (for {
        organizationID <- organizationID
        pendingTraderRequests <- pendingTraderRequests(organizationID)
      } yield Ok(views.html.component.master.organizationViewPendingTraderRequestList(pendingTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewPendingTraderRequest(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewPendingTraderRequest(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedTraderRequestList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def rejectedTraderRequests(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationRejectedTraderRequestList(organizationID)

      (for {
        organizationID <- organizationID
        rejectedTraderRequests <- rejectedTraderRequests(organizationID)
      } yield Ok(views.html.component.master.organizationViewRejectedTraderRequestList(rejectedTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRejectedTraderRequest(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetVerifiedOrganizationID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      (for {
        organizationID <- organizationID
        trader <- trader
      } yield Ok(views.html.component.master.organizationViewRejectedTraderRequest(trader = trader))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewTraderAccountList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTraderAccountList()))
  }

  def zoneViewAcceptedTraderAccountList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def acceptedTraders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      (for {
        zoneID <- zoneID
        acceptedTraders <- acceptedTraders(zoneID)
      } yield Ok(views.html.component.master.zoneViewAcceptedTraderAccountList(acceptedTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedTraderAccount(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewAcceptedTraderAccount(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingTraderRequestList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def pendingTraderRequests(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZonePendingTraderRequestList(zoneID)

      (for {
        zoneID <- zoneID
        pendingTraderRequests <- pendingTraderRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewPendingTraderRequestList(pendingTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingTraderRequest(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewPendingTraderRequest(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedTraderRequestList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def rejectedTraderRequests(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneRejectedTraderRequestList(zoneID)

      (for {
        zoneID <- zoneID
        rejectedTraderRequests <- rejectedTraderRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewRejectedTraderRequestList(rejectedTraderRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedTraderRequest(traderID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val trader = masterTraders.Service.tryGet(traderID)

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewRejectedTraderRequest(trader = trader, organization = organization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewOrganizationAccountList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewOrganizationAccountList()))
  }

  def zoneViewAcceptedOrganizationAccountList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def acceptedOrganizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      (for {
        zoneID <- zoneID
        acceptedOrganizations <- acceptedOrganizations(zoneID)
      } yield Ok(views.html.component.master.zoneViewAcceptedOrganizationAccountList(acceptedOrganizations))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedOrganizationAccount(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val ubos = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        ubos <- ubos
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewAcceptedOrganizationAccount(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingOrganizationRequestList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def pendingOrganizationRequests(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZonePendingOrganizationRequestList(zoneID)

      (for {
        zoneID <- zoneID
        pendingOrganizationRequests <- pendingOrganizationRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewPendingOrganizationRequestList(pendingOrganizationRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewPendingOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)
      val ubos = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        ubos <- ubos
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewPendingOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedOrganizationRequestList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def rejectedOrganizationRequests(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneRejectedOrganizationRequestList(zoneID)

      (for {
        zoneID <- zoneID
        rejectedOrganizationRequests <- rejectedOrganizationRequests(zoneID)
      } yield Ok(views.html.component.master.zoneViewRejectedOrganizationRequestList(rejectedOrganizationRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRejectedOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)

      def ubos(organizationID: String) = masterOrganizationUBOs.Service.getUBOs(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
        ubos <- ubos(organization.id)
      } yield Ok(views.html.component.master.zoneViewRejectedOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs, ubos = ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationSubscription(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationSubscription()))
  }

  def traderSubscription(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderSubscription()))
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

  def userViewPendingRequests: Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val mobile: Future[Option[Mobile]] = masterMobiles.Service.get(loginState.username)
      val email: Future[Option[Email]] = masterEmails.Service.get(loginState.username)
      val identification: Future[Option[Identification]] = masterIdentifications.Service.get(loginState.username)

      def getOrganizationZone(organization: Option[Organization]): Future[Option[Zone]] = if (organization.isDefined) masterZones.Service.get(organization.get.zoneID) else Future(None)

      def getTraderOrganization(trader: Option[Trader]): Future[Option[Organization]] = if (trader.isDefined) masterOrganizations.Service.getOrNone(trader.get.organizationID) else Future(None)

      def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) masterOrganizationKYCs.Service.getAllDocuments(organization.get.id) else Future(Seq[OrganizationKYC]())

      def getTraderByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getByAccountID(accountID)

      def getUserResult(identification: Option[Identification], contactStatus: Seq[String]): Future[Result] = {
        val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
        for {
          trader <- getTraderByAccountID(loginState.username)
          traderOrganization <- getTraderOrganization(trader)
          organization <- getOrganizationOrNoneByAccountID(loginState.username)
          organizationZone <- getOrganizationZone(organization)
          organizationKYCs <- getOrganizationKYCsByOrganization(organization)
        } yield Ok(views.html.component.master.userViewPendingRequests(identification = identification, contactStatus = contactStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, trader = trader))
      }

      (for {
        mobile <- mobile
        email <- email
        identification <- identification
        result <- getUserResult(identification, utilities.Contact.getStatus(mobile, email))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewOrganization: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader: Future[Trader] = masterTraders.Service.tryGetByAccountID(loginState.username)

      def getOrganizationByID(id: String): Future[Organization] = masterOrganizations.Service.tryGet(id)

      (for {
        trader <- trader
        traderOrganization <- getOrganizationByID(trader.organizationID)
      } yield Ok(views.html.component.master.traderViewOrganization(traderOrganization))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organization: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization: Future[Organization] = masterOrganizations.Service.tryGetByAccountID(loginState.username)

      def getZone(zoneID: String): Future[Zone] = masterZones.Service.tryGet(zoneID)

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        organization <- organization
        organizationZone <- getZone(organization.zoneID)
        organizationKYCs <- getOrganizationKYCs(organization.id)
      } yield Ok(views.html.component.master.organization(organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderRelationList(): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.traderRelationList())
  }

  def acceptedTraderRelationList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID: Future[String] = masterTraders.Service.tryGetID(loginState.username)

      def acceptedTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllAcceptedTraderRelation(traderID)

      (for {
        traderID <- traderID
        acceptedTraderRelations <- acceptedTraderRelations(traderID)
      } yield Ok(views.html.component.master.acceptedTraderRelationList(acceptedTraderRelationList = acceptedTraderRelations))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def pendingTraderRelationList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID: Future[String] = masterTraders.Service.tryGetID(loginState.username)

      def receivedPendingTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllReceivedPendingTraderRelation(traderID)

      def sentPendingTraderRelations(traderID: String): Future[Seq[TraderRelation]] = masterTraderRelations.Service.getAllSentPendingTraderRelation(traderID)

      (for {
        traderID <- traderID
        receivedPendingTraderRelations <- receivedPendingTraderRelations(traderID)
        sentPendingTraderRelations <- sentPendingTraderRelations(traderID)
      } yield Ok(views.html.component.master.pendingTraderRelationList(sentPendingTraderRelations = sentPendingTraderRelations, receivedPendingTraderRelations = receivedPendingTraderRelations))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def acceptedTraderRelation(fromID: String, toID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val fromTrader = masterTraders.Service.tryGet(fromID)
        val toTrader = masterTraders.Service.tryGet(toID)

        def getResult(fromTrader: Trader, toTrader: Trader): Future[Result] = {
          def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

          loginState.username match {
            case fromTrader.accountID => {
              for {
                organizationName <- getOrganizationName(toTrader.organizationID)
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = toTrader.accountID, organizationName = organizationName))
            }
            case toTrader.accountID => {
              for {
                organizationName <- getOrganizationName(fromTrader.organizationID)
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = fromTrader.accountID, organizationName = organizationName))
            }
            case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
          }
        }

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          result <- getResult(fromTrader = fromTrader, toTrader = toTrader)
        } yield result).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
  }

  def pendingSentTraderRelation(toID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val trader = masterTraders.Service.tryGet(toID)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          trader <- trader
          organizationName <- getOrganizationName(trader.organizationID)
        } yield Ok(views.html.component.master.pendingSentTraderRelation(accountID = trader.accountID, organizationName = organizationName))).recover {
          case baseException: BaseException => InternalServerError(baseException.failure.message)
        }
  }

  def pendingReceivedTraderRelation(fromID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val fromTrader = masterTraders.Service.tryGet(fromID)
        val toTrader = masterTraders.Service.tryGetByAccountID(loginState.username)

        def traderRelation(fromId: String, toId: String): Future[TraderRelation] = masterTraderRelations.Service.tryGet(fromID = fromId, toID = toId)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          traderRelation <- traderRelation(fromId = fromTrader.id, toId = toTrader.id)
          organizationName <- getOrganizationName(fromTrader.organizationID)
        } yield Ok(views.html.component.master.pendingReceivedTraderRelation(traderRelation = traderRelation, counterPartyAccountID = fromTrader.accountID, organizationName = organizationName))).recover {
          case baseException: BaseException => ServiceUnavailable(Html(baseException.failure.message))
        }
  }

  def zoneViewOrganizationBankAccount(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizationBankAccountDetail(organizationZoneID: String, zoneID: String): Future[Option[OrganizationBankAccountDetail]] = if (organizationZoneID == zoneID) {
        masterOrganizationBankAccountDetails.Service.get(organizationID)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        organizationZoneID <- organizationZoneID
        zoneID <- zoneID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationZoneID = organizationZoneID, zoneID = zoneID)
      } yield Ok(views.html.component.master.zoneViewOrganizationBankAccount(organizationID, organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationBankAccount(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.organizationBankAccount(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewOrganizationBankAccount(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.traderViewOrganizationBankAccount(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def userViewOrganizationUBOs(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)

      def ubos(organization: Option[Organization]) = organization match {
        case Some(organization) => masterOrganizationUBOs.Service.getUBOs(organization.id)
        case None => Future(Seq())
      }

      (for {
        organization <- organization
        ubos <- ubos(organization)
      } yield Ok(views.html.component.master.userViewOrganizationUBOs(organization, ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def viewOrganizationUBOs(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def ubos(organizationID: String) = masterOrganizationUBOs.Service.getUBOs(organizationID)

      (for {
        organizationID <- organizationID
        ubos <- ubos(organizationID)
      } yield Ok(views.html.component.master.viewOrganizationUBOs(ubos))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          Ok(views.html.component.master.traderViewAcceptedNegotiation(traderID = traderID, negotiation = negotiation))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
  }

  def traderViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getAsset(assetID: String) = masterAssets.Service.tryGet(assetID)

      def getOrder(negotiationID: String): Future[Option[Order]] = masterOrders.Service.get(negotiationID)

      def getCounterPartyTrader(traderID: String, negotiation: Negotiation) = masterTraders.Service.tryGet(if (traderID == negotiation.buyerTraderID) negotiation.sellerTraderID else negotiation.buyerTraderID)

      def getBuyerAddress(traderID: String, negotiation: Negotiation, counterPartyAccountID: String) = if (traderID == negotiation.buyerTraderID) Future(loginState.address) else blockchainAccounts.Service.tryGetAddress(counterPartyAccountID)

      def getSellerAddress(traderID: String, negotiation: Negotiation, counterPartyAccountID: String) = if (traderID == negotiation.sellerTraderID) Future(loginState.address) else blockchainAccounts.Service.tryGetAddress(counterPartyAccountID)

      def getSuccessiveTransactionStatus(traderID: String, negotiation: Negotiation, order: Option[Order], buyerAddress: String, sellerAddress: String, asset: Asset) = {
        val pegHash = asset.pegHash.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))

        if (traderID == negotiation.buyerTraderID) {
          negotiation.status match {
            case constants.Status.Negotiation.CONTRACT_SIGNED | constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING => blockchainTransactionConfirmBuyerBids.Service.getTransactionStatus(buyerAddress, sellerAddress, pegHash)
            case constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED => order match {
              case Some(order) => order.status match {
                case constants.Status.Order.ASSET_AND_FIAT_PENDING | constants.Status.Order.ASSET_SENT_FIAT_PENDING => blockchainTransactionSendFiats.Service.getTransactionStatus(buyerAddress, sellerAddress, pegHash)
                case constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING | constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING => if (!asset.moderated) blockchainTransactionBuyerExecuteOrders.Service.getTransactionStatus(buyerAddress, sellerAddress, pegHash) else Future(Option(false))
                case _ => Future(Option(false))
              }
              case None => blockchainTransactionSendFiats.Service.getTransactionStatus(buyerAddress, sellerAddress, pegHash)
            }
            case _ => Future(Option(false))
          }
        } else if (traderID == negotiation.sellerTraderID) {
          negotiation.status match {
            case constants.Status.Negotiation.CONTRACT_SIGNED | constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING => blockchainTransactionConfirmSellerBids.Service.getTransactionStatus(sellerAddress, buyerAddress, pegHash)
            case constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED => order match {
              case Some(order) => order.status match {
                case constants.Status.Order.ASSET_AND_FIAT_PENDING | constants.Status.Order.FIAT_SENT_ASSET_PENDING => blockchainTransactionSendAssets.Service.getTransactionStatus(sellerAddress, buyerAddress, pegHash)
                case constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING | constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING => if (!asset.moderated) blockchainTransactionSellerExecuteOrders.Service.getTransactionStatus(buyerAddress, sellerAddress, pegHash) else Future(Option(false))
                case _ => Future(Option(false))
              }
              case None => blockchainTransactionSendAssets.Service.getTransactionStatus(sellerAddress, buyerAddress, pegHash)
            }
            case _ => Future(Option(false))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }

      }

      def getResult(traderID: String, negotiation: Negotiation, order: Option[Order], asset: Asset, counterPartyTrader: Trader, successiveTransactionStatus: Option[Boolean]) = {
        if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
          val counterPartyOrganization = masterOrganizations.Service.tryGet(counterPartyTrader.organizationID)
          for {
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.traderViewAcceptedNegotiationTerms(traderID = traderID, counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization, negotiation = negotiation, order = order, asset = asset, successiveTransactionStatus = successiveTransactionStatus))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        asset <- getAsset(negotiation.assetID)
        order <- getOrder(negotiation.id)
        counterPartyTrader <- getCounterPartyTrader(traderID, negotiation)
        buyerAddress <- getBuyerAddress(traderID, negotiation, counterPartyTrader.accountID)
        sellerAddress <- getSellerAddress(traderID, negotiation, counterPartyTrader.accountID)
        successiveTransactionStatus <- getSuccessiveTransactionStatus(traderID, negotiation, order, buyerAddress, sellerAddress, asset)
        result <- getResult(traderID, negotiation, order, asset, counterPartyTrader, successiveTransactionStatus)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
      } yield {
        if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
          Ok(views.html.component.master.traderViewCompletedNegotiation(negotiationHistory = negotiationHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
  }

  def traderViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiationHistory: NegotiationHistory) = {
        if (traderID == negotiationHistory.buyerTraderID || traderID == negotiationHistory.sellerTraderID) {
          val assetHistory = masterAssetHistories.Service.tryGet(negotiationHistory.assetID)
          val counterPartyTrader = masterTraders.Service.tryGet(if (traderID == negotiationHistory.buyerTraderID) negotiationHistory.sellerTraderID else negotiationHistory.buyerTraderID)

          def getCounterPartyOrganization(organizationID: String) = masterOrganizations.Service.tryGet(organizationID)

          for {
            assetHistory <- assetHistory
            counterPartyTrader <- counterPartyTrader
            counterPartyOrganization <- getCounterPartyOrganization(counterPartyTrader.organizationID)
          } yield Ok(views.html.component.master.traderViewCompletedNegotiationTerms(counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(traderID, negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiation(negotiationID = negotiationID)))
  }

  def organizationViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(traderList: Seq[Trader], negotiation: Negotiation, organizationID: String) = {
        if (traderList.map(_.organizationID) contains organizationID) {
          val getAsset = masterAssets.Service.tryGet(negotiation.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(traderList.find(_.organizationID != organizationID).map(_.organizationID).getOrElse(""))
          for {
            asset <- getAsset
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewAcceptedNegotiationTerms(traderList = traderList, counterPartyOrganization = counterPartyOrganization, negotiation = negotiation, asset = asset))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderList, negotiation, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedNegotiation(negotiationID = negotiationID)))
  }

  def organizationViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(traderList: Seq[Trader], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderList.map(_.organizationID) contains organizationID) {
          val assetHistory = masterAssetHistories.Service.tryGet(negotiationHistory.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(traderList.find(_.organizationID != organizationID).map(_.organizationID).getOrElse(""))
          for {
            assetHistory <- assetHistory
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewCompletedNegotiationTerms(traderList = traderList, counterPartyOrganization = counterPartyOrganization, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderList <- getTraderList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderList, negotiationHistory, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiation: Negotiation) = {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          val negotiationEnvelopeList = docusignEnvelopes.Service.getAll(negotiationID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
            negotiationEnvelopeList <- negotiationEnvelopeList
          } yield {
            Ok(views.html.component.master.traderViewNegotiationDocumentList(traderID = traderID, negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList, negotiationEnvelopeList = negotiationEnvelopeList))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
          } yield Ok(views.html.component.master.organizationViewNegotiationDocumentList(negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiation: Negotiation, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
          } yield Ok(views.html.component.master.zoneViewNegotiationDocumentList(negotiation = negotiation, assetFileList = assetFileList, negotiationFileList = negotiationFileList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        negotiation <- negotiation
        zoneID <- zoneID
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderZoneIDs, negotiation, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocument(negotiationID: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(negotiation: Negotiation, traderID: String) = if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
        documentType match {
          case Some(documentType) =>
            documentType match {
              case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                for {
                  assetFile <- assetFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, assetFile))
              case _ =>
                val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                for {
                  negotiationFile <- negotiationFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, negotiationFile))
            }
          case None =>
            val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            for {
              assetFileList <- assetFileList
              negotiationFileList <- negotiationFileList
            } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
        }
      }
      else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(negotiation, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocument(negotiationID: String, documentTypeOrNone: Option[String]): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          documentTypeOrNone match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              for {
                assetFileList <- assetFileList
                negotiationFileList <- negotiationFileList
              } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocument(negotiationID: String, documentType: Option[String]): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiation: Negotiation, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              for {
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
              } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(traderZoneIDs, negotiation, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedNegotiationDocument(negotiationID: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(negotiationHistory: NegotiationHistory, traderID: String) = if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
        documentType match {
          case Some(documentType) =>
            documentType match {
              case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                for {
                  assetFile <- assetFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, assetFile))
              case _ =>
                val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                for {
                  negotiationFile <- negotiationFile
                } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, negotiationFile))
            }
          case None =>
            val assetFileList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
            val negotiationFileList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
            for {
              assetFileList <- assetFileList
              negotiationFileList <- negotiationFileList
            } yield Ok(views.html.component.master.traderViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
        }
      }
      else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(negotiationHistory, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationDocument(negotiationID: String, documentTypeOrNone: Option[String]): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          documentTypeOrNone match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val assetFileList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
              val negotiationFileList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
              for {
                assetFileList <- assetFileList
                negotiationFileList <- negotiationFileList
              } yield Ok(views.html.component.master.organizationViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiationHistory, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiationDocument(negotiationID: String, documentType: Option[String]): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiationHistory: NegotiationHistory, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA =>
                  val assetFile = masterTransactionAssetFileHistories.Service.get(negotiationHistory.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, assetFile))
                case _ =>
                  val negotiationFile = masterTransactionNegotiationFileHistories.Service.get(negotiationID, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, negotiationFile))
              }
            case None =>
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiationHistory.assetID)
              for {
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
              } yield Ok(views.html.component.master.zoneViewNegotiationDocument(negotiationID, (assetFileList ++ negotiationFileList).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiationHistory.buyerTraderID, negotiationHistory.sellerTraderID))
        result <- getResult(traderZoneIDs, negotiationHistory, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tradeDocuments(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(negotiation: Negotiation, traderID: String) = {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
          val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          val negotiationEnvelopeList = docusignEnvelopes.Service.getAll(negotiationID)
          for {
            negotiationFileList <- negotiationFileList
            assetFileList <- assetFileList
            negotiationEnvelopeList <- negotiationEnvelopeList
          } yield Ok(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(negotiation, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield Ok(views.html.component.master.traderViewAcceptedNegotiationDocumentList(negotiationID, traderID, negotiation))
  }

  def traderViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getResult(traderID: String, negotiationHistory: NegotiationHistory) = {
        if (negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield {
            Ok(views.html.component.master.traderViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        negotiationHistory <- negotiationHistory
        result <- getResult(traderID, negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiationHistory: NegotiationHistory, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield Ok(views.html.component.master.organizationViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiationHistory <- negotiationHistory
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiationHistory, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiationHistory: NegotiationHistory, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          val negotiationFileHistoryList = masterTransactionNegotiationFileHistories.Service.getAllDocuments(negotiationID)
          val assetFileHistoryList = masterTransactionAssetFileHistories.Service.getAllDocuments(negotiationHistory.assetID)
          for {
            negotiationFileHistoryList <- negotiationFileHistoryList
            assetFileHistoryList <- assetFileHistoryList
          } yield Ok(views.html.component.master.zoneViewCompletedNegotiationDocumentList(negotiationHistory = negotiationHistory, assetFileHistoryList = assetFileHistoryList, negotiationFileHistoryList = negotiationFileHistoryList))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
        result <- getResult(traderZoneIDs, negotiationHistory, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiationDocumentList(negotiationID)))
  }

  def zoneViewAcceptedNegotiationDocumentList(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiationDocumentList(negotiationID)))
  }

  //Dashboard Cards

  def organizationTradeStatistics(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def getSellTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderIDs(traderIDs)

      def getSellTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderIDs(traderIDs)

      def getBuyTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderIDs(traderIDs)

      def getBuyTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderIDs(traderIDs)

      (for {
        organizationID <- organizationID
        traderList <- getTraders(organizationID)
        sellTradeList <- getSellTradeList(traderList.map(_.id))
        sellTradeHistoryList <- getSellTradeHistoryList(traderList.map(_.id))
        buyTraderList <- getBuyTradeList(traderList.map(_.id))
        buyTradeHistoryList <- getBuyTradeHistoryList(traderList.map(_.id))
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.organizationTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderTradeStatistics(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getSellTradeList(traderID: String) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderID(traderID)

      def getSellTradeHistoryList(traderID: String) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderID(traderID)

      def getBuyTradeList(traderID: String) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderID(traderID)

      def getBuyTradeHistoryList(traderID: String) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderID(traderID)

      (for {
        traderID <- traderID
        sellTradeList <- getSellTradeList(traderID)
        sellTradeHistoryList <- getSellTradeHistoryList(traderID)
        buyTraderList <- getBuyTradeList(traderID)
        buyTradeHistoryList <- getBuyTradeHistoryList(traderID)
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.traderTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneTradeStatistics(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def getSellTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListBySellerTraderIDs(traderIDs)

      def getSellTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListBySellerTraderIDs(traderIDs)

      def getBuyTradeList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllContractSignedNegotiationListByBuyerTraderIDs(traderIDs)

      def getBuyTradeHistoryList(traderIDs: Seq[String]) = masterNegotiationHistories.Service.getAllCompletedNegotiationListByBuyerTraderIDs(traderIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraders(zoneID)
        sellTradeList <- getSellTradeList(traderList.map(_.id))
        sellTradeHistoryList <- getSellTradeHistoryList(traderList.map(_.id))
        buyTraderList <- getBuyTradeList(traderList.map(_.id))
        buyTradeHistoryList <- getBuyTradeHistoryList(traderList.map(_.id))
      } yield {
        val currentYear = Year.now().getValue
        val currentMonth = Calendar.getInstance.get(Calendar.MONTH)
        val timePeriods = (1 to 12).map { x => Seq(if (x - 1 <= currentMonth) currentYear else currentYear - 1, new DecimalFormat("00").format(x)).mkString("-") }.sorted

        val sellTradesMonthly = timePeriods.map { timePeriod =>
          sellTradeList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + sellTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        val buyTradesMonthly = timePeriods.map { timePeriod =>
          buyTraderList.count(x => x.updatedOn.getOrElse("").toString.slice(0, 7) == timePeriod) + buyTradeHistoryList.count(x => x.updatedOn.getOrElse(x.createdOn.getOrElse("")).toString.slice(0, 7) == timePeriod)
        }
        Ok(views.html.component.master.zoneTradeStatistics(timePeriods, buyTradesMonthly, sellTradesMonthly))
      }
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationDeclarations(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationDeclarations()))
  }

  // zone trades cards

  def zoneViewActiveNegotiationList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderList(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersByZoneID(zoneID)

      def getAcceptedNegotiationList(traderIDs: Seq[String]) = masterNegotiations.Service.getAllAcceptedNegotiationListByTraderIDs(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getAssetListList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraderList(zoneID)
        acceptedNegotiationList <- getAcceptedNegotiationList(traderList.map(_.id))
        counterPartyTraderList <- getCounterPartyTraderList(traderList.map(_.id))
        assetList <- getAssetListList(acceptedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.zoneViewActiveNegotiationList(acceptedNegotiationList, counterPartyTraderList, assetList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedNegotiationList: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.zoneViewAcceptedNegotiationList())
  }

  def zoneViewCompletedNegotiationList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderList(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersByZoneID(zoneID)

      def getCompletedNegotiationList(traderIDs: Seq[String]): Future[Seq[NegotiationHistory]] = masterNegotiationHistories.Service.getAllCompletedNegotiationListByTraderIDs(traderIDs)

      def getCounterPartyTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getAssetList(assetIDs: Seq[String]): Future[Seq[AssetHistory]] = masterAssetHistories.Service.getAllAssetsByID(assetIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraderList(zoneID)
        completedNegotiationList <- getCompletedNegotiationList(traderList.map(_.id))
        counterPartyTraderList <- getCounterPartyTraderList(completedNegotiationList.map(negotiation => if (traderList.map(_.id) contains negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID))
        assetList <- getAssetList(completedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.zoneViewCompletedNegotiationList(completedNegotiationList, counterPartyTraderList, assetList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAcceptedNegotiation(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiation(negotiationID = negotiationID)))
  }

  def zoneViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiation: Negotiation) = if (traderList.map(_.zoneID) contains zoneID) {
        val asset = masterAssets.Service.tryGet(negotiation.assetID)
        val organizationList = masterOrganizations.Service.getOrganizations(traderList.map(_.organizationID))
        for {
          asset <- asset
          organizationList <- organizationList
        } yield Ok(views.html.component.master.zoneViewAcceptedNegotiationTerms(traderList = traderList, organizationList = organizationList, negotiation = negotiation, asset = asset))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID = zoneID, traderList = traderList, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedNegotiation(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewCompletedNegotiation(negotiationID = negotiationID)))
  }

  def zoneViewCompletedNegotiationTerms(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiationHistory: NegotiationHistory): Future[Result] = if (traderList.map(_.zoneID) contains zoneID) {
        val assetHistory = masterAssetHistories.Service.tryGet(negotiationHistory.assetID)
        val organizationList = masterOrganizations.Service.getOrganizations(traderList.map(_.organizationID))
        for {
          assetHistory <- assetHistory
          organizationList <- organizationList
        } yield Ok(views.html.component.master.zoneViewCompletedNegotiationTerms(traderList = traderList, organizationList = organizationList, negotiationHistory = negotiationHistory, assetHistory = assetHistory))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiationHistory <- negotiationHistory
        traderList <- getTraderList(Seq(negotiationHistory.buyerTraderID, negotiationHistory.sellerTraderID))
        result <- getResult(zoneID = zoneID, traderList = traderList, negotiationHistory = negotiationHistory)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def zoneViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def zoneViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)

      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
      } yield Ok(views.html.component.master.zoneViewTradeRoomFinancial(fiatsInOrder, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def zoneViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraderIDList(zoneID: String) = masterTraders.Service.getVerifiedTraderIDsByZoneID(zoneID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiationHistory <- negotiationHistory
        fiatsInOrderHistory <- fiatsInOrderHistory
        zoneID <- zoneID
        traderIDList <- getTraderIDList(zoneID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.zoneViewCompletedTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiationHistory.price - fiatsInOrderHistory))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def zoneViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getBillOfLading(assetID: String): Future[Option[AssetFile]] = masterTransactionAssetFiles.Service.get(assetID, constants.File.Asset.BILL_OF_LADING)

      (for {
        negotiation <- negotiation
        billOfLading <- getBillOfLading(negotiation.assetID)
      } yield Ok(views.html.component.master.zoneViewTradeRoomChecks(negotiation.assetID, billOfLading.map(document => document.documentContent.map(_.asInstanceOf[BillOfLading].vesselName))))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)

      def billOfLading(assetID: String): Future[Option[AssetFileHistory]] = masterTransactionAssetFileHistories.Service.get(assetID, constants.File.Asset.BILL_OF_LADING)

      (for {
        negotiationHistory <- negotiationHistory
        billOfLading <- billOfLading(negotiationHistory.assetID)
      } yield Ok(views.html.component.master.zoneViewCompletedTradeRoomChecks(negotiationHistory.assetID, billOfLading.map(document => document.documentContent.map(_.asInstanceOf[BillOfLading].vesselName))))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.organizationViewTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationHistory = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderIDList(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def getFiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      (for {
        negotiationHistory <- negotiationHistory
        fiatsInOrderHistory <- fiatsInOrderHistory
        organizationID <- organizationID
        traderIDList <- getTraderIDList(organizationID)
        fiatPegWallet <- getFiatPegWallet(traderIDList)
      } yield Ok(views.html.component.master.organizationViewTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiationHistory.price - fiatsInOrderHistory))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomChecks()))
  }

  def organizationViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewCompletedTradeRoomChecks()))
  }

  //TODO: Whoever did this correct it
  def traderViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomFinancialAndChecks(negotiationID)))
  }


  def traderViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getAsset(assetID: String) = masterAssets.Service.tryGet(assetID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
        fiatPegWallet <- getFiatPegWallet(traderID)
        asset <- getAsset(negotiation.assetID)
      } yield Ok(views.html.component.master.traderViewTradeRoomFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, amountPaid = 0, amountPending = (negotiation.price - fiatsInOrder), traderID = traderID, moderated = asset.moderated))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompletedTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewCompletedTradeRoomFinancialAndChecks(negotiationID)))
  }

  def traderViewCompletedTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiationHistories.Service.tryGet(negotiationID)
      val fiatsInOrderHistory = masterTransactionSendFiatRequestHistories.Service.getFiatsInOrder(negotiationID)

      def getFiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def getAsset(assetID: String) = masterAssetHistories.Service.tryGet(assetID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        fiatsInOrderHistory <- fiatsInOrderHistory
        fiatPegWallet <- getFiatPegWallet(traderID)
        asset <- getAsset(negotiation.assetID)
      } yield Ok(views.html.component.master.traderViewCompletedTradeRoomFinancial(fiatPegWallet.map(_.transactionAmount).sum, 0, negotiation.price - fiatsInOrderHistory, traderID, asset.moderated))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //TODO: Whoever did this correct it
  def traderViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomChecks()))
  }

  def traderViewCompletedTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewCompletedTradeRoomChecks()))
  }

  def zoneOrderActions(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (traderList.map(_.zoneID) contains zoneID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          val order = masterOrders.Service.get(negotiationID)
          for {
            asset <- asset
            order <- order
          } yield Ok(views.html.component.master.zoneViewOrderActions(negotiation, asset, order))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID, traderList, negotiation)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionNegotiationFiles.Service.get(negotiationID, documentType)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getDocumentContent(document: Option[NegotiationFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionNegotiationFileHistories.Service.getDocumentContent(negotiationID, documentType)
        }
      }

      (for {
        document <- document
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent, negotiation, traderList, organizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val document = masterTransactionAssetFiles.Service.get(assetID, documentType)

      def getDocumentContent(document: Option[AssetFile]) = {
        if (document.isDefined) {
          Future(document.get.documentContent)
        } else {
          masterTransactionAssetFileHistories.Service.getDocumentContent(assetID, documentType)
        }
      }

      (for {
        document <- document
        documentContent <- getDocumentContent(document)
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewIssueFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getIssueFiatRequestList(traderID: String) = westernUnionFiatRequests.Service.getAll(traderID)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        traderID <- traderID
        issueFiatRequestList <- getIssueFiatRequestList(traderID)
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.traderViewIssueFiatRequestList(issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewIssueFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getTraderList(organizationID: String) = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def getIssueFiatRequestList(traderIDs: Seq[String]) = westernUnionFiatRequests.Service.getAllByTraderIDs(traderIDs)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        organizationID <- organizationID
        traderList <- getTraderList(organizationID)
        issueFiatRequestList <- getIssueFiatRequestList(traderList.map(_.id))
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.organizationViewIssueFiatRequestList(traderList, issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewIssueFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getOrganizationList(zoneID: String) = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def getTraderList(zoneID: String) = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def getIssueFiatRequestList(traderIDs: Seq[String]) = westernUnionFiatRequests.Service.getAllByTraderIDs(traderIDs)

      def getRTCBList(issueFiatRequestIDs: Seq[String]) = westernUnionRTCBs.Service.getAll(issueFiatRequestIDs)

      (for {
        zoneID <- zoneID
        organizationList <- getOrganizationList(zoneID)
        traderList <- getTraderList(zoneID)
        issueFiatRequestList <- getIssueFiatRequestList(traderList.map(_.id))
        rtcbList <- getRTCBList(issueFiatRequestList.map(_.id))
      } yield Ok(views.html.component.master.zoneViewIssueFiatRequestList(traderList, organizationList, issueFiatRequestList, rtcbList))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transaction View Cards
  def zoneViewSendFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewSendFiatRequests()))
  }

  def zoneViewPendingSendFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewPendingSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewCompleteSendFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewFailedSendFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewRedeemFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewRedeemFiatRequests()))
  }

  def zoneViewPendingRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewPendingRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }


  def zoneViewCompleteRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }


  def zoneViewFailedRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewRedeemFiatRequestList(redeemFiatRequestList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def zoneViewReceiveFiatList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def organizations(zoneID: String): Future[Seq[Organization]] = masterOrganizations.Service.getZoneAcceptedOrganizationList(zoneID)

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def receiveFiatList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderIDs)

      def receiveFiatHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderIDs).map(_.map(_.convertToReceiveFiat))

      (for {
        zoneID <- zoneID
        organizations <- organizations(zoneID)
        traders <- traders(zoneID)
        receiveFiatList <- receiveFiatList(traders.map(_.id))
        receiveFiatHistoryList <- receiveFiatHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList, organizations, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transactions organization
  def organizationViewSendFiatRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewSendFiatRequests()))
  }

  def organizationViewPendingSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompleteSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewFailedSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      def sendFiatRequestHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderIDs).map(_.map(_.convertToSendFiatRequest))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traders.map(_.id))
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRedeemFiatRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewRedeemFiatRequests()))
  }

  def organizationViewPendingRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewCompleteRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewFailedRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewRedeemFiatRequestList(redeemFiatRequestList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewReceiveFiatList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traders(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def receiveFiatList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderIDs)

      def receiveFiatHistoryList(traderIDs: Seq[String]): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderIDs).map(_.map(_.convertToReceiveFiat))

      (for {
        organizationID <- organizationID
        traders <- traders(organizationID)
        receiveFiatList <- receiveFiatList(traders.map(_.id))
        receiveFiatHistoryList <- receiveFiatHistoryList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList, traders))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  //Transactions Trader
  def traderViewSendFiatRequests: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewSendFiatRequests()))
  }

  def traderViewPendingSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getPendingSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompleteSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getCompleteSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewFailedSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderID)

      def sendFiatRequestHistoryList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequestHistories.Service.getFailedSendFiatRequests(traderID).map(_.map(_.convertToSendFiatRequest))

      (for {
        trader <- trader
        sendFiatRequestList <- sendFiatRequestList(trader.id)
        sendFiatRequestHistoryList <- sendFiatRequestHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList, sendFiatRequestHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewRedeemFiatRequests: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewRedeemFiatRequests()))
  }

  def traderViewPendingRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewCompleteRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewFailedRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderID)

      (for {
        trader <- trader
        redeemFiatRequestList <- redeemFiatRequestList(trader.id)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewReceiveFiatList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

      def receiveFiatList(traderID: String): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiats.Service.get(traderID)

      def receiveFiatHistoryList(traderID: String): Future[Seq[masterTransaction.ReceiveFiat]] = masterTransactionReceiveFiatHistories.Service.get(traderID).map(_.map(_.convertToReceiveFiat))

      (for {
        trader <- trader
        receiveFiatList <- receiveFiatList(trader.id)
        receiveFiatHistoryList <- receiveFiatHistoryList(trader.id)
      } yield Ok(views.html.component.master.traderViewReceiveFiatList(receiveFiatList, receiveFiatHistoryList))).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(trader: Trader, negotiation: Negotiation): Future[Result] = {
        if (trader.id == negotiation.buyerTraderID || trader.id == negotiation.sellerTraderID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          val counterPartyTrader = masterTraders.Service.tryGet(if (trader.id == negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID)

          def getCounterPartyOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

          for {
            asset <- asset
            counterPartyTrader <- counterPartyTrader
            counterPartyOrganization <- getCounterPartyOrganization(counterPartyTrader.organizationID)
          } yield Ok(views.html.component.master.traderViewNegotiation(negotiation = negotiation, asset = asset, counterPartyTrader = counterPartyTrader, counterPartyOrganization = counterPartyOrganization))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        trader <- trader
        negotiation <- negotiation
        result <- getResult(trader, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getNegotiationTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getResult(organizationID: String, negotiationTraders: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (negotiationTraders.map(_.organizationID) contains organizationID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          val counterPartyOrganization = masterOrganizations.Service.tryGet(negotiationTraders.map(_.organizationID).filterNot(_ == organizationID).headOption.getOrElse(""))

          for {
            asset <- asset
            counterPartyOrganization <- counterPartyOrganization
          } yield Ok(views.html.component.master.organizationViewNegotiation(negotiation = negotiation, asset = asset, trader = negotiationTraders.find(_.organizationID == organizationID), counterPartyTrader = negotiationTraders.filterNot(_.organizationID == organizationID).headOption, counterPartyOrganization))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        negotiationTraders <- getNegotiationTraders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(organizationID, negotiationTraders, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def kycList: Action[AnyContent] = withoutLoginAction {
    implicit request => Ok(views.html.component.master.kycList())
  }

  def kycOngoingList: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)
      val kycList = worldCheckKycs.Service.get(loginState.username)

      (for {
        identification <- identification
        kycList <- kycList
      }yield Ok(views.html.component.master.kycOngoingList(kycList = kycList, identification = identification))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def kycReceivedList: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val identification = masterIdentifications.Service.get(loginState.username)
      val kycList = worldCheckKycs.Service.get(loginState.username)

      (for {
        identification <- identification
        kycList <- kycList
      }yield  Ok(views.html.component.master.kycReceivedList(kycList = kycList, identification = identification))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }

  }
}