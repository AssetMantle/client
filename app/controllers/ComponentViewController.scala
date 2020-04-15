package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain._
import models.master.{Asset, Identification, Negotiation, Organization, OrganizationBankAccountDetail, OrganizationKYC, Trader, TraderKYC, TraderRelation, Zone}
import models.masterTransaction.{AssetFile, IssueAssetRequest}
import models.master.{Organization => _, Zone => _, _}
import models.masterTransaction.{AssetFile, IssueAssetRequest, Notification}
import models.{blockchain, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(
                                         actorsCreate: actors.Create,
                                         messagesControllerComponents: MessagesControllerComponents,
                                         masterTraders: master.Traders, masterAccountKYC: master.AccountKYCs,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterTraderKYCs: master.TraderKYCs,
                                         masterAssets: master.Assets,
                                         masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories,
                                         masterTransactionNotifications: masterTransaction.Notifications,
                                         masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                         masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                         masterNegotiations: master.Negotiations,
                                         masterAccounts: master.Accounts,
                                         masterAccountFiles: master.AccountFiles,
                                         blockchainAssets: blockchain.Assets,
                                         blockchainFiats: blockchain.Fiats,
                                         blockchainNegotiations: blockchain.Negotiations,
                                         masterOrganizations: master.Organizations,
                                         masterZones: master.Zones,
                                         blockchainOrders: blockchain.Orders,
                                         blockchainAccounts: blockchain.Accounts,
                                         masterAccountKYCs: master.AccountKYCs,
                                         masterIdentifications: master.Identifications,
                                         masterTraderRelations: master.TraderRelations,
                                         masterTraderBackgroundChecks: master.TraderBackgroundChecks,
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         withZoneLoginAction: WithZoneLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withLoginAction: WithLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

  private val notificationsPerPageLimit = configuration.get[Int]("notification.notificationsPerPage")

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (loginState.userType match {
        case constants.User.UNKNOWN =>
          val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
          for {
            profilePicture <- profilePicture
          } yield Ok(views.html.component.master.commonHome(profilePicture = profilePicture))
        case _ =>
          val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
          val coins = blockchainAccounts.Service.getCoins(loginState.address)
          for {
            profilePicture <- profilePicture
            coins <- coins
          } yield Ok(views.html.component.master.commonHome(coins, profilePicture))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        buyNegotiationList <- buyNegotiationList(traderID)
        assetsList <- assetsList(buyNegotiationList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(buyNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, assets = assetsList, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewAcceptedSellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def sellNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllAcceptedSellNegotiationListByTraderID(traderID)

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        sellNegotiationList <- sellNegotiationList(traderID)
        assetsList <- assetsList(sellNegotiationList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(sellNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, assets = assetsList, counterPartyTraders = counterPartyTraders))
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

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        sentNegotiationRequestList <- sentNegotiationRequestList(traderID)
        assetsList <- assetsList(sentNegotiationRequestList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(sentNegotiationRequestList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, assets = assetsList, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewReceivedNegotiationRequestList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def receivedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllReceivedNegotiationListByTraderID(traderID)

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        receivedNegotiationList <- receivedNegotiationList(traderID)
        assetsList <- assetsList(receivedNegotiationList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(receivedNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewReceivedNegotiationRequestList(receivedNegotiationRequestList = receivedNegotiationList, assets = assetsList, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewIncompleteNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def incompleteNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllIncompleteNegotiationListByTraderID(traderID)

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        incompleteNegotiationList <- incompleteNegotiationList(traderID)
        assetsList <- assetsList(incompleteNegotiationList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(incompleteNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, assets = assetsList, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def traderViewRejectedAndFailedNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def buyerRejectedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderID(traderID)

      def sellerRejectedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderID(traderID)

      def failedNegotiationList(traderID: String): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderID(traderID)

      def assetsList(assetIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getAllAssetsByID(assetIDs)

      def counterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        buyerRejectedNegotiationList <- buyerRejectedNegotiationList(traderID)
        sellerRejectedNegotiationList <- sellerRejectedNegotiationList(traderID)
        failedNegotiationList <- failedNegotiationList(traderID)
        assetsList <- assetsList(buyerRejectedNegotiationList.map(_.assetID) ++ sellerRejectedNegotiationList.map(_.assetID) ++ failedNegotiationList.map(_.assetID))
        counterPartyTraders <- counterPartyTraders(buyerRejectedNegotiationList.map(_.buyerTraderID) ++ sellerRejectedNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.traderViewRejectedAndFailedNegotiationList(rejectedNegotiationList = buyerRejectedNegotiationList ++ sellerRejectedNegotiationList, failedNegotiationList = failedNegotiationList, assets = assetsList, counterPartyTraders = counterPartyTraders))
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

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        buyNegotiationList <- getBuyNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(buyNegotiationList.map(_.sellerTraderID))
      } yield Ok(views.html.component.master.organizationViewAcceptedBuyNegotiationList(buyNegotiationList = buyNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders))
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

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        sellNegotiationList <- getSellNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(sellNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.organizationViewAcceptedSellNegotiationList(sellNegotiationList = sellNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders))
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

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        sentNegotiationRequestList <- getSentNegotiationRequestList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(sentNegotiationRequestList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.organizationViewSentNegotiationRequestList(sentNegotiationRequestList = sentNegotiationRequestList, traders = traders, counterPartyTraders = counterPartyTraders))
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

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        receivedNegotiationList <- getReceivedNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(receivedNegotiationList.map(_.sellerTraderID))
      } yield Ok(views.html.component.master.organizationViewReceivedNegotiationList(receivedNegotiationList = receivedNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders))
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

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        incompleteNegotiationList <- getIncompleteNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(incompleteNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.organizationViewIncompleteNegotiationList(incompleteNegotiationList = incompleteNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewRejectedAndFailedNegotiationList: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getOrganizationTradersIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getBuyerRejectedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListByBuyerTraderIDs(traderIDs)

      def getSellerRejectedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllRejectedNegotiationListBySellerTraderIDs(traderIDs)

      def getFailedNegotiationList(traderIDs: Seq[String]): Future[Seq[Negotiation]] = masterNegotiations.Service.getAllFailedNegotiationListBySellerTraderIDs(traderIDs)

      def getTraders(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        organizationID <- organizationID
        organizationTradersIDs <- getOrganizationTradersIDs(organizationID)
        buyerRejectedNegotiationList <- getBuyerRejectedNegotiationList(organizationTradersIDs)
        sellerRejectedNegotiationList <- getSellerRejectedNegotiationList(organizationTradersIDs)
        failedNegotiationList <- getFailedNegotiationList(organizationTradersIDs)
        traders <- getTraders(organizationTradersIDs)
        counterPartyTraders <- getCounterPartyTraders(buyerRejectedNegotiationList.map(_.sellerTraderID) ++ sellerRejectedNegotiationList.map(_.buyerTraderID) ++ failedNegotiationList.map(_.buyerTraderID))
      } yield Ok(views.html.component.master.organizationViewRejectedAndFailedNegotiationList(buyerRejectedNegotiationList = buyerRejectedNegotiationList, sellerRejectedNegotiationList = sellerRejectedNegotiationList, failedNegotiationList = failedNegotiationList, traders = traders, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def recentActivityForOrganization(pageNumber: Int = 0): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def tradersInOrganizations(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getOrganizationAcceptedTraderList(organizationID)

      def getTradersNotifications(traderAccountIDs: Seq[String]): Future[Seq[Notification]] = masterTransactionNotifications.Service.getTradersNotifications(traderAccountIDs, pageNumber * notificationsPerPageLimit, notificationsPerPageLimit)

      (for {
        organizationID <- organizationID
        tradersInOrganizations <- tradersInOrganizations(organizationID)
        tradersNotifications <- getTradersNotifications(tradersInOrganizations.map(_.accountID))
      } yield Ok(views.html.component.master.recentActivities(tradersNotifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.recentActivityForOrganization), None))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def recentActivityForTrader(pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val notifications = masterTransactionNotifications.Service.get(loginState.username, pageNumber * notificationsPerPageLimit, notificationsPerPageLimit)
      (for {
        notifications <- notifications
      } yield Ok(views.html.component.master.recentActivities(notifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.recentActivityForTrader), None))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def traderViewRecentActivityForTradeRoom(pageNumber: Int = 0, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeActivities = masterTransactionTradeActivities.Service.getTradeActivity(negotiationID)

      def notifications(ids: Seq[String]): Future[Seq[Notification]] = masterTransactionNotifications.Service.getTradeRoomNotifications(loginState.username, ids, pageNumber * notificationsPerPageLimit, notificationsPerPageLimit)

      (for {
        tradeActivities <- tradeActivities
        notifications <- notifications(tradeActivities.map(_.notificationID))
      } yield Ok(views.html.component.master.recentActivities(notifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.traderViewRecentActivityForTradeRoom), Option(negotiationID)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def organizationViewRecentActivityForTradeRoom(pageNumber: Int = 0, negotiationID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeActivities = masterTransactionTradeActivities.Service.getTradeActivity(negotiationID)

      def notifications(ids: Seq[String]): Future[Seq[Notification]] = masterTransactionNotifications.Service.getTradeRoomNotifications(loginState.username, ids, pageNumber * notificationsPerPageLimit, notificationsPerPageLimit)

      (for {
        tradeActivities <- tradeActivities
        notifications <- notifications(tradeActivities.map(_.notificationID))
      } yield Ok(views.html.component.master.recentActivities(notifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.traderViewRecentActivityForTradeRoom), Option(negotiationID)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def comet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(actorsCreate.Service.cometSource(loginState.username) via Comet.json("parent.cometMessage")).as(ContentTypes.HTML))
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
      val accountStatus: Future[String] = masterAccounts.Service.getStatus(loginState.username)

      def identification(accountID: String): Future[Option[Identification]] = masterIdentifications.Service.get(accountID)

      def getZoneOrNoneByOrganization(organization: Option[Organization]): Future[Option[Zone]] = if (organization.isDefined) masterZones.Service.getOrNone(organization.get.zoneID) else Future(None)

      def getOrganizationOrNoneByTrader(trader: Option[Trader]): Future[Option[Organization]] = if (trader.isDefined) masterOrganizations.Service.getOrNone(trader.get.organizationID) else Future(None)

      def getTraderKYCsByTrader(trader: Option[Trader]): Future[Seq[TraderKYC]] = if (trader.isDefined) masterTraderKYCs.Service.getAllDocuments(trader.get.id) else Future(Seq[TraderKYC]())

      def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) masterOrganizationKYCs.Service.getAllDocuments(organization.get.id) else Future(Seq[OrganizationKYC]())

      def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getOrNoneByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getByAccountID(accountID)

      def getUserResult(identification: Option[Identification], accountStatus: String): Future[Result] = {
        val identificationStatus = if (identification.isDefined) identification.get.verificationStatus.getOrElse(false) else false
        if (identificationStatus && accountStatus == constants.Status.Account.COMPLETE) {
          for {
            trader <- getTraderOrNoneByAccountID(loginState.username)
            traderOrganization <- getOrganizationOrNoneByTrader(trader)
            traderKYCs <- getTraderKYCsByTrader(trader)
            organization <- getOrganizationOrNoneByAccountID(loginState.username)
            organizationZone <- getZoneOrNoneByOrganization(organization)
            organizationKYCs <- getOrganizationKYCsByOrganization(organization)
          } yield Ok(views.html.component.master.userViewPendingRequests(identification = identification, accountStatus = accountStatus, organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs, traderOrganization = traderOrganization, trader = trader, traderKYCs = traderKYCs))
        } else {
          Future(Ok(views.html.component.master.userViewPendingRequests(identification = if (identification.isDefined) Option(identification.get) else None, accountStatus = accountStatus)))
        }
      }

      (for {
        accountStatus <- accountStatus
        identification <- identification(loginState.username)
        result <- getUserResult(identification, accountStatus)
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

      def getZone(zoneID: String): Future[Zone] = masterZones.Service.get(zoneID)

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

      def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        asset <- getAsset(negotiation.assetID)
      } yield {
        if (traderID == negotiation.buyerTraderID || traderID == negotiation.sellerTraderID) {
          Ok(views.html.component.master.traderViewAcceptedNegotiationTerms(traderID = traderID, negotiation = negotiation, asset = asset))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }
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

      def getOrganizationTraderIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        organizationTraderIDs <- getOrganizationTraderIDs(organizationID)
        asset <- getAsset(negotiation.assetID)
      } yield {
        if (organizationTraderIDs.contains(negotiation.buyerTraderID) || organizationTraderIDs.contains(negotiation.sellerTraderID)) {
          Ok(views.html.component.master.organizationViewAcceptedNegotiationTerms(negotiation = negotiation, asset = asset))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationFiles(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def checkTraderNegotiationExists(traderID: String) = masterNegotiations.Service.checkTraderNegotiationExists(id, traderID)

      def getResult(traderNegotiationExists: Boolean) = {
        if (traderNegotiationExists) {
          val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
          for {
            negotiationFiles <- negotiationFiles
          } yield Ok(views.html.component.master.traderViewNegotiationFiles(id = id, files = negotiationFiles))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        checkTraderNegotiationExists <- checkTraderNegotiationExists(traderID)
        result <- getResult(checkTraderNegotiationExists)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewNegotiationFiles(id: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getOrganizationTraderIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getResult(organizationTraderIDs: Seq[String], negotiation: Negotiation) = {
        if (organizationTraderIDs.contains(negotiation.buyerTraderID) || organizationTraderIDs.contains(negotiation.sellerTraderID)) {
          val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
          for {
            negotiationFiles <- negotiationFiles
          } yield Ok(views.html.component.master.organizationViewNegotiationFiles(id = id, files = negotiationFiles))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        organizationTraderIDs <- getOrganizationTraderIDs(organizationID)
        result <- getResult(organizationTraderIDs, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def traderViewNegotiationFile(id: String, documentType: Option[String] = None): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def checkTraderNegotiationExists(traderID: String) = masterNegotiations.Service.checkTraderNegotiationExists(id, traderID)

      def getResult(traderNegotiationExists: Boolean) = {
        if (traderNegotiationExists) {
          documentType match {
            case Some(documentType) =>
              val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, documentType)
              for {
                negotiationFile <- negotiationFile
              } yield Ok(views.html.component.master.traderViewNegotiationFile(negotiationFile))
            case None =>
              val negotiationDocuments = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
              for {
                negotiationDocuments <- negotiationDocuments
              } yield Ok(views.html.component.master.traderViewNegotiationFile(negotiationDocuments.headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        checkTraderNegotiationExists <- checkTraderNegotiationExists(traderID)
        result <- getResult(checkTraderNegotiationExists)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }

  }

  def organizationViewNegotiationFile(id: String, documentType: Option[String]): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getOrganizationTraderIDs(organizationID: String) = masterTraders.Service.getTraderIDsByOrganizationID(organizationID)

      def getResult(organizationTraderIDs: Seq[String], negotiation: Negotiation) = {
        if (organizationTraderIDs.contains(negotiation.buyerTraderID) || organizationTraderIDs.contains(negotiation.sellerTraderID)) {
          documentType match {
            case Some(documentType) =>
              val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, documentType)
              for {
                negotiationFile <- negotiationFile
              } yield Ok(views.html.component.master.organizationViewNegotiationFile(negotiationFile))
            case None =>
              val negotiationDocuments = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
              for {
                negotiationDocuments <- negotiationDocuments
              } yield Ok(views.html.component.master.organizationViewNegotiationFile(negotiationDocuments.headOption))
          }
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        organizationID <- organizationID
        negotiation <- negotiation
        organizationTraderIDs <- getOrganizationTraderIDs(organizationID)
        result <- getResult(organizationTraderIDs, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def negotiationDocumentUpload(id: String) = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(id)
      for {
        negotiationFiles <- negotiationFiles
      } yield Ok(views.html.component.master.negotiationDocumentUpload(id, negotiationFiles))
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

  def organizationDeclarations(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.organizationDeclarations()))
  }
}