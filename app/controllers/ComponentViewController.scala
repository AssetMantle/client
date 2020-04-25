package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Asset, Identification, Negotiation, Organization, OrganizationBankAccountDetail, OrganizationKYC, Trader, TraderKYC, TraderRelation, Zone}
import models.master.{Organization => _, Zone => _, _}
import models.{blockchain, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html
import models.Trait.Document
import models.masterTransaction.AssetFiles

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         messagesControllerComponents: MessagesControllerComponents,
                                         masterTraders: master.Traders,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterTraderKYCs: master.TraderKYCs,
                                         masterAssets: master.Assets,
                                         masterTransactionNotifications: masterTransaction.Notifications,
                                         masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                         masterNegotiations: master.Negotiations,
                                         masterAccounts: master.Accounts,
                                         masterAccountFiles: master.AccountFiles,
                                         blockchainFiats: blockchain.Fiats,
                                         masterOrganizations: master.Organizations,
                                         masterZones: master.Zones,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterMobiles: master.Mobiles,
                                         masterEmails: master.Emails,
                                         masterIdentifications: master.Identifications,
                                         masterTraderRelations: master.TraderRelations,
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                         withUsernameToken: results.WithUsernameToken,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         withZoneLoginAction: WithZoneLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withLoginAction: WithLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
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
      val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(loginState.address)
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def negotiations(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllConfirmedNegotiationListByTraderID(traderID)

      //If we make a master.Order Table and store order status such ASSET_SEND, FIAT_SEND, ORDER_COMPLETE, etc. then fetch incomplete orders and take difference w.r.t negotiations

      (for {
        fiatPegWallet <- fiatPegWallet
        traderID <- traderID
        negotiations <- negotiations(traderID)
      } yield Ok(views.html.component.master.traderFinancials(walletBalance = fiatPegWallet.map(_.transactionAmount.toInt).sum, payable = negotiations.filter(_.buyerTraderID == traderID).map(_.price).sum, receivable = negotiations.filter(_.sellerTraderID == traderID).map(_.price).sum))
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

      def buyNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        buyNegotiationList <- buyNegotiationList(traderID)
        counterPartyTraders <- counterPartyTraders(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewAcceptedSellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def sellNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        sellNegotiationList <- sellNegotiationList(traderID)
        counterPartyTraders <- counterPartyTraders(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
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

      def sentNegotiationRequestList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        sentNegotiationRequestList <- sentNegotiationRequestList(traderID)
        counterPartyTraders <- counterPartyTraders(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewReceivedNegotiationRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def receivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        receivedNegotiationList <- receivedNegotiationList(traderID)
        counterPartyTraders <- counterPartyTraders(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewReceivedNegotiationRequestList(receivedNegotiationRequestList = receivedNegotiationList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewIncompleteNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def incompleteNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        incompleteNegotiationList <- incompleteNegotiationList(traderID)
        counterPartyTraders <- counterPartyTraders(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.traderViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewRejectedAndFailedNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def rejectedReceivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderID(traderID)

      def rejectedSentNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderID(traderID)

      def failedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderID(traderID)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        traderID <- traderID
        rejectedReceivedNegotiationList <- rejectedReceivedNegotiationList(traderID)
        rejectedSentNegotiationList <- rejectedSentNegotiationList(traderID)
        failedNegotiationList <- failedNegotiationList(traderID)
        counterPartyTraders <- counterPartyTraders(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.traderViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
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

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getBuyNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedBuyNegotiationListByTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        buyNegotiationList <- getBuyNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(buyNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(buyNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewAcceptedSellNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSellNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        sellNegotiationList <- getSellNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(sellNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(sellNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
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

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getSentNegotiationRequestList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllSentNegotiationRequestListByTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        sentNegotiationRequestList <- getSentNegotiationRequestList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(sentNegotiationRequestList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(sentNegotiationRequestList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewReceivedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        receivedNegotiationList <- getReceivedNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(receivedNegotiationList.map(_.sellerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(receivedNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewReceivedNegotiationList(receivedNegotiationList = receivedNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewIncompleteNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getIncompleteNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        incompleteNegotiationList <- getIncompleteNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(incompleteNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets(incompleteNegotiationList.map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewRejectedAndFailedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getRejectedReceivedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs)

      def getRejectedSentNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderIDs(traderIDs)

      def getFailedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def counterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def assets(assetIDs: Seq[String]) = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        rejectedReceivedNegotiationList <- getRejectedReceivedNegotiationList(organizationTradersIDs)
        rejectedSentNegotiationList <- getRejectedSentNegotiationList(organizationTradersIDs)
        failedNegotiationList <- getFailedNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(rejectedReceivedNegotiationList.map(_.sellerTraderID) ++ rejectedSentNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
        counterPartyOrganizations <- counterPartyOrganizations(counterPartyTraders.map(_.organizationID))
        assets <- assets((rejectedReceivedNegotiationList ++ rejectedSentNegotiationList ++ failedNegotiationList).map(_.assetID))
      } yield Ok(views.html.component.master.organizationViewRejectedAndFailedNegotiationList(rejectedReceivedNegotiationList = rejectedReceivedNegotiationList, rejectedSentNegotiationList = rejectedSentNegotiationList, failedNegotiationList = failedNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders, counterPartyOrganizations = counterPartyOrganizations, assets = assets))
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

      def getTraderKYCs(organizationID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.organizationID == organizationID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        organizationID <- organizationID
        trader <- trader
        traderKYCs <- getTraderKYCs(organizationID = organizationID, trader = trader)
      } yield Ok(views.html.component.master.organizationViewAcceptedTraderAccount(trader = trader, traderKYCs = traderKYCs))
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

      def getTraderKYCs(organizationID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.organizationID == organizationID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        organizationID <- organizationID
        trader <- trader
        traderKYCs <- getTraderKYCs(organizationID = organizationID, trader = trader)
      } yield Ok(views.html.component.master.organizationViewPendingTraderRequest(trader = trader, traderKYCs = traderKYCs))
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

      def getTraderKYCs(organizationID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.organizationID == organizationID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        organizationID <- organizationID
        trader <- trader
        traderKYCs <- getTraderKYCs(organizationID = organizationID, trader = trader)
      } yield Ok(views.html.component.master.organizationViewRejectedTraderRequest(trader = trader, traderKYCs = traderKYCs))
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

      def getTraderKYCs(zoneID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.zoneID == zoneID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        traderKYCs <- getTraderKYCs(zoneID = zoneID, trader = trader)
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewAcceptedTraderAccount(trader = trader, traderKYCs = traderKYCs, organization = organization))
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

      def getTraderKYCs(zoneID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.zoneID == zoneID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        traderKYCs <- getTraderKYCs(zoneID = zoneID, trader = trader)
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewPendingTraderRequest(trader = trader, traderKYCs = traderKYCs, organization = organization))
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

      def getTraderKYCs(zoneID: String, trader: Trader): Future[Seq[TraderKYC]] = if (trader.zoneID == zoneID) {
        masterTraderKYCs.Service.getAllDocuments(trader.id)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      def organization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

      (for {
        zoneID <- zoneID
        trader <- trader
        traderKYCs <- getTraderKYCs(zoneID = zoneID, trader = trader)
        organization <- organization(trader.organizationID)
      } yield Ok(views.html.component.master.zoneViewRejectedTraderRequest(trader = trader, traderKYCs = traderKYCs, organization = organization))
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
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
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

      def getTraderKYCsByTrader(trader: Option[Trader]): Future[Seq[TraderKYC]] = if (trader.isDefined) masterTraderKYCs.Service.getAllDocuments(trader.get.id) else Future(Seq[TraderKYC]())

      def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) masterOrganizationKYCs.Service.getAllDocuments(organization.get.id) else Future(Seq[OrganizationKYC]())

      def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getOrNoneByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getByAccountID(accountID)

      def getUserResult(identification: Option[Identification], contactStatus: Seq[String]): Future[Result] = {
        val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
        if (identificationStatus && contactStatus.sameElements(Seq(constants.Status.Contact.MOBILE_NUMBER_VERIFIED, constants.Status.Contact.EMAIL_ADDRESS_VERIFIED))) {
          for {
            trader <- getTraderOrNoneByAccountID(loginState.username)
            traderOrganization <- getTraderOrganization(trader)
            traderKYCs <- getTraderKYCsByTrader(trader)
            organization <- getOrganizationOrNoneByAccountID(loginState.username)
            organizationZone <- getOrganizationZone(organization)
            organizationKYCs <- getOrganizationKYCsByOrganization(organization)
          } yield Ok(views.html.component.master.userViewPendingRequests(identification = identification, contactStatus = contactStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, trader = trader, traderKYCs = traderKYCs))
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

  def traderViewAcceptedNegotiation(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          Ok(views.html.component.master.traderViewAcceptedNegotiation(id = id, traderID = traderID, negotiation = negotiation))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
  }

  def traderViewAcceptedNegotiationTerms(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation) = {
        if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
          val getAsset = masterAssets.Service.tryGet(negotiation.assetID)
          for {
            asset <- getAsset
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

  def organizationViewAcceptedNegotiation(id: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiation(id = id)))
  }

  def organizationViewAcceptedNegotiationTerms(id: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def traderOrganizationIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

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
        traderOrganizationIDs <- traderOrganizationIDs(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationFileList(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation) = {
        if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
          val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
          val assetFiles = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)

          for {
            negotiationFiles <- negotiationFiles
            assetFiles <- assetFiles
          } yield Ok(views.html.component.master.traderViewNegotiationFileList(id = id, negotiation = negotiation, assetFiles = assetFiles, negotiationFiles = negotiationFiles))
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

  def organizationViewNegotiationFileList(id: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def traderOrganizationIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
          val assetFiles = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
          for {
            negotiationFiles <- negotiationFiles
            assetFiles <- assetFiles
          } yield Ok(views.html.component.master.organizationViewNegotiationFileList(id = id, negotiation = negotiation, assetFiles = assetFiles, negotiationFiles = negotiationFiles))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- traderOrganizationIDs(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewNegotiationFileList(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(id)
      val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)

      def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

      (for {
        negotiation <- negotiation
        negotiationFiles <- negotiationFiles
        assetFiles <- assetFiles(negotiation.assetID)
      } yield Ok(views.html.component.master.zoneViewNegotiationFileList(id = id, negotiation = negotiation, assetFiles = assetFiles, negotiationFiles = negotiationFiles))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationFile(id: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(negotiation: Negotiation, traderID: String) = if (negotiation.sellerTraderID == traderID || negotiation.buyerTraderID == traderID) {
        documentType match {
          case Some(documentType) =>
            documentType match {
              case constants.File.OBL | constants.File.COO | constants.File.COA =>
                val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                for {
                  assetFile <- assetFile
                } yield Ok(views.html.component.master.traderViewNegotiationFile(id, assetFile))
              case constants.File.INVOICE | constants.File.BILL_OF_EXCHANGE | constants.File.CONTRACT =>
                val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, documentType)
                for {
                  negotiationFile <- negotiationFile
                } yield Ok(views.html.component.master.traderViewNegotiationFile(id, negotiationFile))
            }
          case None =>
            val assetFiles = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
            val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
            for {
              assetFiles <- assetFiles
              negotiationFiles <- negotiationFiles
            } yield Ok(views.html.component.master.traderViewNegotiationFile(id, (assetFiles ++ negotiationFiles).headOption))
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

  def organizationViewNegotiationFile(id: String, documentType: Option[String]): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def traderOrganizationIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

      def getResult(traderOrganizationIDs: Seq[String], negotiation: Negotiation, organizationID: String) = {
        if (traderOrganizationIDs contains organizationID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.OBL | constants.File.COO | constants.File.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationFile(id, assetFile))
                case constants.File.INVOICE | constants.File.BILL_OF_EXCHANGE | constants.File.CONTRACT =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.organizationViewNegotiationFile(id, negotiationFile))
              }
            case None =>
              val assetFiles = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
              for {
                assetFiles <- assetFiles
                negotiationFiles <- negotiationFiles
              } yield Ok(views.html.component.master.organizationViewNegotiationFile(id, (assetFiles ++ negotiationFiles).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        traderOrganizationIDs <- traderOrganizationIDs(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        result <- getResult(traderOrganizationIDs, negotiation, organizationID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def zoneViewNegotiationFile(id: String, documentType: Option[String]): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def traderZoneIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(traderZoneIDs: Seq[String], negotiation: Negotiation, zoneID: String) = {
        if (traderZoneIDs contains zoneID) {
          documentType match {
            case Some(documentType) =>
              documentType match {
                case constants.File.OBL | constants.File.COO | constants.File.COA =>
                  val assetFile = masterTransactionAssetFiles.Service.get(negotiation.assetID, documentType)
                  for {
                    assetFile <- assetFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationFile(id, assetFile))
                case constants.File.INVOICE | constants.File.BILL_OF_EXCHANGE | constants.File.CONTRACT =>
                  val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, documentType)
                  for {
                    negotiationFile <- negotiationFile
                  } yield Ok(views.html.component.master.zoneViewNegotiationFile(id, negotiationFile))
              }
            case None =>
              val assetFiles = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
              for {
                assetFiles <- assetFiles
                negotiationFiles <- negotiationFiles
              } yield Ok(views.html.component.master.zoneViewNegotiationFile(id, (assetFiles ++ negotiationFiles).headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderZoneIDs <- traderZoneIDs(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(traderZoneIDs, negotiation, zoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def tradeDocuments(id: String) = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def assetFiles(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

      (for {
        negotiationFiles <- negotiationFiles
        negotiation <- negotiation
        assetFiles <- assetFiles(negotiation.assetID)
      } yield Ok(views.html.component.master.tradeDocuments(id, negotiation, assetFiles, negotiationFiles))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def traderViewAcceptedNegotiationFiles(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)
      for {
        traderID <- traderID
        negotiation <- negotiation
      } yield Ok(views.html.component.master.traderViewAcceptedNegotiationFiles(id, traderID, negotiation))
  }

  def organizationViewAcceptedNegotiationFiles(id: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewAcceptedNegotiationFiles(id)))
  }

  def zoneViewAcceptedNegotiationFiles(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiationFiles(id)))
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
        traders <- getTraders(organizationID)
        tradeCompletedBuyNegotiationList <- getTradeCompletedBuyNegotiationList(traders.map(_.id))
        tradeCompletedSellNegotiationList <- getTradeCompletedSellNegotiationList(traders.map(_.id))
      } yield Ok(views.html.component.master.organizationTradeStatistics(
        tradeCompletedBuyNegotiationList = tradeCompletedBuyNegotiationList.sortBy(_.time).reverse,
        tradeCompletedSellNegotiationList = tradeCompletedSellNegotiationList.sortBy(_.time).reverse,
        traders = traders,
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
        traders <- getTraders(zoneID)
        tradeCompletedBuyNegotiationList <- getTradeCompletedBuyNegotiationList(traders.map(_.id))
        tradeCompletedSellNegotiationList <- getTradeCompletedSellNegotiationList(traders.map(_.id))
      } yield Ok(views.html.component.master.zoneTradeStatistics(
        tradeCompletedBuyNegotiationList = tradeCompletedBuyNegotiationList.sortBy(_.time).reverse,
        tradeCompletedSellNegotiationList = tradeCompletedSellNegotiationList.sortBy(_.time).reverse,
        traders = traders,
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

      def traders(zoneID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersByZoneID(zoneID)

      def acceptedNegotiations(traderIDs: Seq[String]) = masterNegotiations.Service.getAllAcceptedNegotiationListByTraderIDs(traderIDs)

      def counterPartyTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      (for {
        zoneID <- zoneID
        traders <- traders(zoneID)
        acceptedNegotiations <- acceptedNegotiations(traders.map(_.id))
        counterPartyTraders <- counterPartyTraders(traders.map(_.id))
        assetList <- assetsList(acceptedNegotiations.map(_.assetID))
      } yield Ok(views.html.component.master.zoneViewAcceptedNegotiationList(acceptedNegotiations, counterPartyTraders, assetList))).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewAcceptedNegotiation(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewAcceptedNegotiation(id = id)))
  }

  def zoneViewAcceptedNegotiationTerms(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def traderZoneIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      def getResult(zoneID: String, traderZoneIDs: Seq[String], negotiation: Negotiation, asset: Asset): Result = if (traderZoneIDs contains zoneID) {
        Ok(views.html.component.master.zoneViewAcceptedNegotiationTerms(negotiation = negotiation, asset = asset))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traderZoneIDs <- traderZoneIDs(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        asset <- getAsset(negotiation.assetID)
      } yield getResult(zoneID = zoneID, traderZoneIDs = traderZoneIDs, negotiation = negotiation, asset = asset)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def zoneViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: show correct FINANCIALS after orderTable
      Future(Ok(views.html.component.master.zoneViewTradeRoomFinancial(0, 0, 0)))
  }

  def zoneViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneViewTradeRoomChecks()))
  }


  def organizationViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def organizationViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: show correct FINANCIALS after orderTable
      Future(Ok(views.html.component.master.organizationViewTradeRoomFinancial(0, 0, 0)))
  }

  def organizationViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationViewTradeRoomChecks()))
  }


  def traderViewTradeRoomFinancialAndChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomFinancialAndChecks(negotiationID)))
  }

  def traderViewTradeRoomFinancial(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      //TODO: show correct FINANCIALS after orderTable
      Future(Ok(views.html.component.master.traderViewTradeRoomFinancial(0, 0, 0)))
  }

  def traderViewTradeRoomChecks(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.traderViewTradeRoomChecks()))
  }


  def zoneOrderActions(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def traders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getResult(zoneID: String, traders: Seq[Trader], negotiation: Negotiation): Future[Result] = {
        if (traders.map(_.zoneID) contains zoneID) {
          def asset(assetID: String) = masterAssets.Service.tryGet(assetID)

          for {
            asset <- asset(negotiation.assetID)
          } yield Ok(views.html.component.master.zoneViewOrderActions(zoneID, traders, negotiation, asset))
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        traders <- traders(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID, traders, negotiation)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationDocumentContent(id: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewAssetDocumentContent(id: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewNegotiationDocumentContent(id: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewAssetDocumentContent(id: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewNegotiationDocumentContent(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewNegotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def zoneViewAssetDocumentContent(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionAssetFiles.Service.getDocumentContent(id, documentType)

      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.viewAssetDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

}