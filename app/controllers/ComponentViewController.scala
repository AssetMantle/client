package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.docusign
import models.master.{Asset, Identification, Negotiation, Organization, OrganizationBankAccountDetail, OrganizationKYC, Trader, TraderRelation, Zone}
import models.master.{Organization => _, Zone => _, _}
import models.{blockchain, master, masterTransaction, westernUnion}
import models.masterTransaction.SendFiatRequest
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         messagesControllerComponents: MessagesControllerComponents,
                                         blockchainFiats: blockchain.Fiats,
                                         masterAssets: master.Assets,
                                         masterAccountFiles: master.AccountFiles,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterEmails: master.Emails,
                                         masterIdentifications: master.Identifications,
                                         masterMobiles: master.Mobiles,
                                         masterNegotiations: master.Negotiations,
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterOrganizations: master.Organizations,
                                         masterTraders: master.Traders,
                                         masterTraderRelations: master.TraderRelations,
                                         masterZones: master.Zones,
                                         masterFiats: master.Fiats,
                                         masterOrders: master.Orders,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         docusignEnvelopes: docusign.Envelopes,
                                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                         masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                         masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                         westernUnionFiatRequests: westernUnion.FiatRequests,
                                         westernUnionRTCBs: westernUnion.RTCBs,
                                         withLoginAction: WithLoginAction,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
                                         withZoneLoginAction: WithZoneLoginAction,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderFinancials: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def fiatPegWallet(traderID: String) = masterFiats.Service.getFiatPegWallet(traderID)

      def negotiations(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderID(traderID)

      def incompleteNegotiations(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def completedOrders(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def fiatsInOrders(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def payable(traderID: String, fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        incompleteNegotiations(negotiations.filter(_.buyerTraderID == traderID), completedOrders).map(_.price).sum - fiatsInOrders.filter(_.traderID == traderID).map(_.amount).sum
      }

      def receivable(incompleteNegotiations: Seq[Negotiation], traderID: String) = incompleteNegotiations.filter(_.sellerTraderID == traderID).map(_.price).sum


      (for {
        traderID <- traderID
        fiatPegWallet <- fiatPegWallet(traderID)
        negotiations <- negotiations(traderID)
        completedOrders <- completedOrders(negotiations.map(_.negotiationID.getOrElse("")))
        fiatsInOrders <- fiatsInOrders(incompleteNegotiations(negotiations, completedOrders).map(_.id))
      } yield Ok(views.html.component.master.traderFinancials(walletBalance = fiatPegWallet.map(_.transactionAmount).sum, payable = payable(traderID, fiatsInOrders, completedOrders, negotiations), receivable = receivable(incompleteNegotiations(negotiations, completedOrders), traderID)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationFinancial: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String) = masterTraders.Service.getVerifiedTraderIDsByOrganizationID(organizationID)

      def fiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def negotiations(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def incompleteNegotiations(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def completedOrders(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def fiatsInOrders(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def payable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        incompleteNegotiations(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def receivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum


      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        fiatPegWallet <- fiatPegWallet(traderIDs)
        negotiations <- negotiations(traderIDs)
        completedOrders <- completedOrders(negotiations.map(_.negotiationID.getOrElse("")))
        fiatsInOrders <- fiatsInOrders(incompleteNegotiations(negotiations, completedOrders).map(_.id))
      } yield Ok(views.html.component.master.organizationFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount.toInt).sum, payable = payable(traderIDs, fiatsInOrders, completedOrders, negotiations), receivable = receivable(incompleteNegotiations(negotiations, completedOrders), traderIDs)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneFinancial: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String) = masterTraders.Service.getVerifiedTraderIDsByZoneID(zoneID)

      def fiatPegWallet(traderIDs: Seq[String]) = masterFiats.Service.getFiatPegWallet(traderIDs)

      def negotiations(traderIDs: Seq[String]) = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderIDs(traderIDs)

      def incompleteNegotiations(negotiations: Seq[Negotiation], completedOrders: Seq[Order]) = negotiations.filterNot(completedOrders.map(_.orderID) contains _.negotiationID.getOrElse(""))

      def completedOrders(orderIDs: Seq[String]) = masterOrders.Service.getCompletedOrdersByOrderIDs(orderIDs)

      def fiatsInOrders(masterNegotiationIDs: Seq[String]) = masterTransactionSendFiatRequests.Service.getFiatRequestsInOrders(masterNegotiationIDs)

      def payable(traderIDs: Seq[String], fiatsInOrders: Seq[SendFiatRequest], completedOrders: Seq[Order], negotiations: Seq[Negotiation]) = {
        val buyingNegotiations = negotiations.filter(traderIDs contains _.buyerTraderID)
        incompleteNegotiations(buyingNegotiations, completedOrders).map(_.price).sum - fiatsInOrders.filter(buyingNegotiations.map(_.id) contains _.negotiationID).filter(traderIDs contains _.traderID).map(_.amount).sum
      }

      def receivable(incompleteNegotiations: Seq[Negotiation], traderIDs: Seq[String]) = incompleteNegotiations.filter(traderIDs contains _.sellerTraderID).map(_.price).sum


      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        fiatPegWallet <- fiatPegWallet(traderIDs)
        negotiations <- negotiations(traderIDs)
        completedOrders <- completedOrders(negotiations.map(_.negotiationID.getOrElse("")))
        fiatsInOrders <- fiatsInOrders(incompleteNegotiations(negotiations, completedOrders).map(_.id))
      } yield Ok(views.html.component.master.zoneFinancial(walletBalance = fiatPegWallet.map(_.transactionAmount.toInt).sum, payable = payable(traderIDs, fiatsInOrders, completedOrders, negotiations), receivable = receivable(incompleteNegotiations(negotiations, completedOrders), traderIDs)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewAcceptedNegotiationList: Action[AnyContent] = Action { implicit request =>
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = Action { implicit request =>
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewAcceptedNegotiationList: Action[AnyContent] = Action { implicit request =>
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewSentReceivedIncompleteRejectedFailedNegotiationList: Action[AnyContent] = Action { implicit request =>
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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

  def comet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(actors.Service.Comet.createSource(loginState.username) via Comet.json("parent.cometMessage")).as(ContentTypes.HTML))
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
      }
  }

  def zoneViewAcceptedOrganizationAccount(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewAcceptedOrganizationAccount(organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
      }
  }

  def zoneViewPendingOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewPendingOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case _: BaseException => InternalServerError(views.html.account())
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
        case _: BaseException => InternalServerError(views.html.account())
      }
  }

  def zoneViewRejectedOrganizationRequest(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val organization = masterOrganizations.Service.tryGet(organizationID)

      def getOrganizationKYCs(zoneID: String, organization: Organization): Future[Seq[OrganizationKYC]] = if (organization.zoneID == zoneID) {
        masterOrganizationKYCs.Service.getAllDocuments(organization.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        organization <- organization
        organizationKYCs <- getOrganizationKYCs(zoneID = zoneID, organization = organization)
      } yield Ok(views.html.component.master.zoneViewRejectedOrganizationRequest(organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case _: BaseException => InternalServerError(views.html.account())
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

      def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getOrNoneByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getByAccountID(accountID)

      def getUserResult(identification: Option[Identification], contactStatus: Seq[String]): Future[Result] = {
        val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
        if (identificationStatus && contactStatus.equals(Seq(constants.Status.Contact.MOBILE_NUMBER_VERIFIED, constants.Status.Contact.EMAIL_ADDRESS_VERIFIED))) {
          for {
            trader <- getTraderOrNoneByAccountID(loginState.username)
            traderOrganization <- getTraderOrganization(trader)
            organization <- getOrganizationOrNoneByAccountID(loginState.username)
            organizationZone <- getOrganizationZone(organization)
            organizationKYCs <- getOrganizationKYCsByOrganization(organization)
          } yield Ok(views.html.component.master.userViewPendingRequests(identification = identification, contactStatus = contactStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, trader = trader))
        } else {
          Future(Ok(views.html.component.master.userViewPendingRequests(identification = if (identification.isDefined) Option(identification.get) else None, contactStatus = contactStatus)))
        }
      }

      (for {
        mobile <- mobile
        email <- email
        identification <- identification
        result <- getUserResult(identification, utilities.Contact.getStatus(mobile, email))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderRelationList(): Action[AnyContent] = Action { implicit request =>
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = toTrader.accountID, traderName = toTrader.name, organizationName = organizationName))
            }
            case toTrader.accountID => {
              for {
                organizationName <- getOrganizationName(fromTrader.organizationID)
              } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = fromTrader.accountID, traderName = fromTrader.name, organizationName = organizationName))
            }
            case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
          }
        }

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          result <- getResult(fromTrader = fromTrader, toTrader = toTrader)
        } yield result).recover {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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
        } yield Ok(views.html.component.master.pendingSentTraderRelation(accountID = trader.accountID, traderName = trader.name, organizationName = organizationName))).recover {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
  }

  def pendingReceivedTraderRelation(fromID: String): Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        val fromTrader = masterTraders.Service.tryGet(fromID)
        val toTrader = masterTraders.Service.tryGetByAccountID(loginState.username)

        def traderRelation(fromId: String, toId: String): Future[TraderRelation] = masterTraderRelations.Service.get(fromID = fromId, toID = toId)

        def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

        (for {
          fromTrader <- fromTrader
          toTrader <- toTrader
          traderRelation <- traderRelation(fromId = fromTrader.id, toId = toTrader.id)
          organizationName <- getOrganizationName(fromTrader.organizationID)
        } yield Ok(views.html.component.master.pendingReceivedTraderRelation(traderRelation = traderRelation, traderName = toTrader.name, organizationName = organizationName))).recover {
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def userViewOrganizationUBOs(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.getByAccountID(loginState.username)

      (for {
        organization <- organization
      } yield Ok(views.html.component.master.userViewOrganizationUBOs(organization))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationUBOs(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization = masterOrganizations.Service.tryGetByAccountID(loginState.username)

      (for {
        organization <- organization
      } yield Ok(views.html.component.master.viewOrganizationUBOs(organization))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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

      def getResult(traderID: String, negotiation: Negotiation) = {
        if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          for {
            asset <- asset
          } yield Ok(views.html.component.master.traderViewAcceptedNegotiationTerms(traderID = traderID, negotiation = negotiation, asset = asset))
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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

      def getTradersOrganizationIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val getAsset = masterAssets.Service.tryGet(negotiation.assetID)
          for {
            asset <- getAsset
          } yield Ok(views.html.component.master.organizationViewAcceptedNegotiationTerms(negotiation = negotiation, asset = asset))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- getTradersOrganizationIDList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
              case constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE | constants.File.Negotiation.CONTRACT =>
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
                case constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE | constants.File.Negotiation.CONTRACT =>
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
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
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
                case constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE | constants.File.Negotiation.CONTRACT =>
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
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
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

      def getTradeCompletedBuyNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs)

      def getTradeCompletedSellNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs)

      (for {
        organizationID <- organizationID
        traderList <- getTraders(organizationID)
        tradeCompletedBuyNegotiationList <- getTradeCompletedBuyNegotiationList(traderList.map(_.id))
        tradeCompletedSellNegotiationList <- getTradeCompletedSellNegotiationList(traderList.map(_.id))
      } yield Ok(views.html.component.master.organizationTradeStatistics(
        tradeCompletedBuyNegotiationList = tradeCompletedBuyNegotiationList.sortBy(_.time).reverse,
        tradeCompletedSellNegotiationList = tradeCompletedSellNegotiationList.sortBy(_.time).reverse,
        traderList = traderList,
      ))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
      }
  }

  def traderTradeStatistics(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getTradeCompletedBuyNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedBuyNegotiationListByTraderID(traderID)

      def getTradeCompletedSellNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedSellNegotiationListByTraderID(traderID)

      (for {
        traderID <- traderID
        tradeCompletedBuyNegotiationList <- getTradeCompletedBuyNegotiationList(traderID)
        tradeCompletedSellNegotiationList <- getTradeCompletedSellNegotiationList(traderID)
      } yield Ok(views.html.component.master.traderTradeStatistics(
        tradeCompletedBuyNegotiationList = tradeCompletedBuyNegotiationList.sortBy(_.time).reverse,
        tradeCompletedSellNegotiationList = tradeCompletedSellNegotiationList.sortBy(_.time).reverse
      ))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
      }
  }

  def zoneTradeStatistics(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def getTraders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getZoneAcceptedTraderList(zoneID)

      def getTradeCompletedBuyNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedBuyNegotiationListByTraderIDs(traderIDs)

      def getTradeCompletedSellNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllTradeCompletedSellNegotiationListByTraderIDs(traderIDs)

      (for {
        zoneID <- zoneID
        traderList <- getTraders(zoneID)
        tradeCompletedBuyNegotiationList <- getTradeCompletedBuyNegotiationList(traderList.map(_.id))
        tradeCompletedSellNegotiationList <- getTradeCompletedSellNegotiationList(traderList.map(_.id))
      } yield Ok(views.html.component.master.zoneTradeStatistics(
        tradeCompletedBuyNegotiationList = tradeCompletedBuyNegotiationList.sortBy(_.time).reverse,
        tradeCompletedSellNegotiationList = tradeCompletedSellNegotiationList.sortBy(_.time).reverse,
        traderList = traderList,
      ))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
      }
  }

  def organizationDeclarations(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationDeclarations()))
  }

  // zone trades cards
  def zoneViewAcceptedNegotiationList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
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
      } yield Ok(views.html.component.master.zoneViewAcceptedNegotiationList(acceptedNegotiationList, counterPartyTraderList, assetList))).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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

      def getTradersZoneIDList(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      def getResult(zoneID: String, traderZoneIDs: Seq[String], negotiation: Negotiation, asset: Asset): Result = if (traderZoneIDs contains zoneID) {
        Ok(views.html.component.master.zoneViewAcceptedNegotiationTerms(negotiation = negotiation, asset = asset))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderZoneIDs <- getTradersZoneIDList(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        asset <- getAsset(negotiation.assetID)
      } yield getResult(zoneID = zoneID, traderZoneIDs = traderZoneIDs, negotiation = negotiation, asset = asset)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
      } yield Ok(views.html.component.master.zoneViewTradeRoomFinancial(negotiation.price, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID, failures = Seq(baseException.failure)))
      }
  }

  //TODO: Whoever did this correct it
  def zoneViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTradeRoomChecks()))
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)
      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
      } yield Ok(views.html.component.master.organizationViewTradeRoomFinancial(negotiation.price, 0, negotiation.price - fiatsInOrder))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID, failures = Seq(baseException.failure)))
      }
  }

  //TODO: Whoever did this correct it
  def organizationViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomChecks()))
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
      (for {
        traderID <- traderID
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
      } yield Ok(views.html.component.master.traderViewTradeRoomFinancial(negotiation.price, 0, negotiation.price - fiatsInOrder, traderID, negotiation))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID, failures = Seq(baseException.failure)))
      }
  }

  //TODO: Whoever did this correct it
  def traderViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomChecks()))
  }

  def zoneOrderActions(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traderList: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (traderList.map(_.zoneID) contains zoneID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          for {
            asset <- asset
          } yield Ok(views.html.component.master.zoneViewOrderActions(zoneID, traderList, negotiation, asset))
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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(assetID, documentType)
      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(assetID, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewNegotiationDocumentContent(negotiationID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewAssetDocumentContent(assetID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(assetID, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.zoneViewPendingSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewCompleteSendFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewFailedSendFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewRedeemFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewRedeemFiatRequests()))
  }

  def zoneViewPendingRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.zoneViewPendingRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }


  def zoneViewCompleteRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }


  def zoneViewFailedRedeemFiatRequestList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderIDs(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDs(zoneID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
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

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewCompleteSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewFailedSendFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def sendFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        sendFiatRequestList <- sendFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewRedeemFiatRequests: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewRedeemFiatRequests()))
  }

  def organizationViewPendingRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewCompleteRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewFailedRedeemFiatRequestList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderIDs(organizationID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def redeemFiatRequestList(traderIDs: Seq[String]): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderIDs)

      (for {
        organizationID <- organizationID
        traderIDs <- traderIDs(organizationID)
        redeemFiatRequestList <- redeemFiatRequestList(traderIDs)
      } yield Ok(views.html.component.master.viewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewSendFiatRequests: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewSendFiatRequests()))
  }

  def traderViewPendingSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getPendingSendFiatRequests(traderID)

      (for {
        traderID <- traderID
        sendFiatRequestList <- sendFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewCompleteSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getCompleteSendFiatRequests(traderID)

      (for {
        traderID <- traderID
        sendFiatRequestList <- sendFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewFailedSendFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def sendFiatRequestList(traderID: String): Future[Seq[masterTransaction.SendFiatRequest]] = masterTransactionSendFiatRequests.Service.getFailedSendFiatRequests(traderID)

      (for {
        traderID <- traderID
        sendFiatRequestList <- sendFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewSendFiatRequestList(sendFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewRedeemFiatRequests: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewRedeemFiatRequests()))
  }

  def traderViewPendingRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getPendingRedeemFiatRequests(traderID)

      (for {
        traderID <- traderID
        redeemFiatRequestList <- redeemFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewCompleteRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getCompleteRedeemFiatRequests(traderID)

      (for {
        traderID <- traderID
        redeemFiatRequestList <- redeemFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewFailedRedeemFiatRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def redeemFiatRequestList(traderID: String): Future[Seq[masterTransaction.RedeemFiatRequest]] = masterTransactionRedeemFiatRequests.Service.getFailedRedeemFiatRequests(traderID)

      (for {
        traderID <- traderID
        redeemFiatRequestList <- redeemFiatRequestList(traderID)
      } yield Ok(views.html.component.master.traderViewRedeemFiatRequestList(redeemFiatRequestList))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiation(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getResult(trader: Trader, negotiation: Negotiation) = {
        if (trader.id == negotiation.buyerTraderID || trader.id == negotiation.sellerTraderID) {
          val asset = masterAssets.Service.tryGet(negotiation.assetID)
          val counterPartyTrader = masterTraders.Service.tryGet(if (trader.id == negotiation.sellerTraderID) negotiation.buyerTraderID else negotiation.sellerTraderID)

          def getCounterPartyOrganization(organizationID: String) = masterOrganizations.Service.tryGet(organizationID)

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
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewNegotiation(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def negotiationTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(organizationID: String, negotiationTraders: Seq[Trader], negotiation: Negotiation) = {
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
        negotiationTraders <- negotiationTraders(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(organizationID, negotiationTraders, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

}