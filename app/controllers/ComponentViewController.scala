package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain._
import models.master.{Identification, Organization, OrganizationBankAccountDetail, OrganizationKYC, Trader, TraderKYC, TraderRelation, Zone}
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
                                         messagesControllerComponents: MessagesControllerComponents,
                                         masterTraders: master.Traders, masterAccountKYC: master.AccountKYCs,
                                         masterOrganizationKYCs: master.OrganizationKYCs,
                                         masterTraderKYCs: master.TraderKYCs,
                                         masterAssets: master.Assets,
                                         masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests,
                                         masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                         blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories,
                                         withOrganizationLoginAction: WithOrganizationLoginAction,
                                         masterTransactionNotifications: masterTransaction.Notifications,
                                         masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                         withZoneLoginAction: WithZoneLoginAction,
                                         withTraderLoginAction: WithTraderLoginAction,
                                         withLoginAction: WithLoginAction,
                                         withUserLoginAction: WithUserLoginAction,
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
                                         masterOrganizationBankAccountDetails: master.OrganizationBankAccountDetails
                                       )(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_COMPONENT_VIEW

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

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

  def genesisDetails: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val address = masterAccounts.Service.getAddress(genesisAccountName)
      (for {
        address <- address
      } yield Ok(views.html.component.master.genesisDetails(address))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (loginState.userType match {
        case constants.User.ZONE =>
          val zone = masterZones.Service.getByAccountID(loginState.username)
          for {
            zone <- zone
          } yield Ok(views.html.component.master.zoneDetails(zone))
        case constants.User.ORGANIZATION =>
          val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)

          def zone(zoneID: String): Future[models.master.Zone] = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield Ok(views.html.component.master.zoneDetails(zone))
        case constants.User.TRADER =>
          val zoneID = masterTraders.Service.getZoneIDByAccountID(loginState.username)

          def zone(zoneID: String): Future[models.master.Zone] = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield Ok(views.html.component.master.zoneDetails(zone))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def assetList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val unapprovedAssets = masterTransactionIssueAssetRequests.Service.getUnapprovedAssets(loginState.username)

      val getBlockchainAssetsPegHashes = blockchainAssets.Service.getAssetPegHashes(loginState.address)

      def assetsWithPegHashes(pegHashes: Seq[String]): Future[Seq[IssueAssetRequest]] = masterTransactionIssueAssetRequests.Service.getAssetsByPegHashes(pegHashes)

      def masterAssetList(pegHashes: Seq[String]): Future[Seq[models.master.Asset]] = masterAssets.Service.getAssetsByPegHashes(pegHashes)

      def allDocumentsForAllAssets(assets: Seq[IssueAssetRequest]): Future[Seq[AssetFile]] = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(assets.map(_.id))

      (for {
        unapprovedAssets <- unapprovedAssets
        blockchainAssetsPegHashes <- getBlockchainAssetsPegHashes
        assetsWithPegHashes <- assetsWithPegHashes(blockchainAssetsPegHashes)
        masterAssetList <- masterAssetList(blockchainAssetsPegHashes)
        allDocumentsForAllAssets <- allDocumentsForAllAssets(unapprovedAssets ++ assetsWithPegHashes)
      } yield Ok(views.html.component.master.assetList(unapprovedAssets ++ assetsWithPegHashes, masterAssetList, allDocumentsForAllAssets))
        ).recover {
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
      val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)

      def orders(negotiations: Seq[Negotiation]): Future[Seq[Order]] = blockchainOrders.Service.getOrders(negotiations.map(_.id))

      def getFiatsInOrders(ordersIDS: Seq[String]): Future[Seq[Fiat]] = blockchainFiats.Service.getFiatPegWallet(ordersIDS)

      def getNegotiationsOfOrders(negotiations: Seq[Negotiation], orders: Seq[Order]): Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)

      def getPayable(negotiationsOfOrders: Seq[Negotiation], fiatsInOrders: Seq[Fiat]): Int = {
        val sumBuying = negotiationsOfOrders.filter(_.buyerAddress == loginState.address).map(_.bid.toInt).sum
        val sumBought = fiatsInOrders.filter(x => negotiationsOfOrders.filter(_.buyerAddress == loginState.address).map(_.id) contains x.ownerAddress).map(_.transactionAmount.toInt).sum
        sumBought - sumBuying
      }

      def getReceivable(negotiationsOfOrders: Seq[Negotiation]): Int = {
        val sumSelling = negotiationsOfOrders.filter(_.sellerAddress == loginState.address).map(_.bid.toInt).sum
        sumSelling
      }

      def walletBalance(fiat: Seq[Fiat]): Int = fiat.map(_.transactionAmount.toInt).sum

      (for {
        fiatPegWallet <- fiatPegWallet
        negotiations <- negotiations
        orders <- orders(negotiations)
        getFiatsInOrders <- getFiatsInOrders(orders.map(_.id))
      } yield Ok(views.html.component.master.traderFinancials(walletBalance(fiatPegWallet), getPayable(getNegotiationsOfOrders(negotiations, orders), getFiatsInOrders), getReceivable(getNegotiationsOfOrders(negotiations, orders))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationsForBuyerAddress = blockchainNegotiations.Service.getNegotiationsForBuyerAddress(loginState.address)
      val assetPegHashes = blockchainAssets.Service.getAssetPegHashes(loginState.address)
      (for {
        negotiationsForBuyerAddress <- negotiationsForBuyerAddress
        assetPegHashes <- assetPegHashes
      } yield Ok(views.html.component.master.buyNegotiationList(negotiationsForBuyerAddress, assetPegHashes))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationsForSellerAddress = blockchainNegotiations.Service.getNegotiationsForSellerAddress(loginState.address)
      val assetPegHashes = blockchainAssets.Service.getAssetPegHashes(loginState.address)
      (for {
        negotiationsForSellerAddress <- negotiationsForSellerAddress
        assetPegHashes <- assetPegHashes
      } yield Ok(views.html.component.master.sellNegotiationList(negotiationsForSellerAddress, assetPegHashes))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def orderList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)

      def orders(negotiations: Seq[Negotiation]): Future[Seq[Order]] = blockchainOrders.Service.getOrders(negotiations.map(_.id))

      def getNegotiationsOfOrders(negotiations: Seq[Negotiation], orders: Seq[Order]): Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)

      def assets(negotiationsOfOrders: Seq[Negotiation]): Future[Seq[models.blockchain.Asset]] = blockchainAssets.Service.getByPegHashes(negotiationsOfOrders.map(_.assetPegHash))

      (for {
        negotiations <- negotiations
        orders <- orders(negotiations)
        assets <- assets(getNegotiationsOfOrders(negotiations, orders))
      } yield {
        val negotiationsOfOrders = getNegotiationsOfOrders(negotiations, orders)
        Ok(views.html.component.master.orderList(orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.buyerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.sellerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id)))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def availableAssetList: Action[AnyContent] = Action.async { implicit request =>
    val masterAssetList = masterAssets.Service.getMarketAssets()

    def assets(pegHashes: Seq[String]) = masterTransactionIssueAssetRequests.Service.getAssetsByPegHashes(pegHashes)

    def allDocumentsForAllAssets(assets: Seq[IssueAssetRequest]): Future[Seq[AssetFile]] = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(assets.map(_.id))

    (for {
      masterAssetList <- masterAssetList
      assets <- assets(masterAssetList.map(_.pegHash))
      allDocumentsForAllAssets <- allDocumentsForAllAssets(assets)
    } yield Ok(views.html.component.master.availableAssetList(assets, masterAssetList, allDocumentsForAllAssets))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val masterAssetList = masterAssets.Service.getMarketAssets()

      def masterTransactionAssets(pegHashes: Seq[String]) = masterTransactionIssueAssetRequests.Service.getAssetsByPegHashes(pegHashes)

      val allOrderIDs = blockchainOrders.Service.getAllOrderIds

      def blockchainAssetList(allOrderIDs: Seq[String]): Future[Seq[models.blockchain.Asset]] = blockchainAssets.Service.getAllPublic(allOrderIDs)

      def allDocumentsForAllAssets(masterTransactionAssets: Seq[IssueAssetRequest]): Future[Seq[AssetFile]] = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(masterTransactionAssets.map(_.id))

      (for {
        masterAssetList <- masterAssetList
        allOrderIDs <- allOrderIDs
        masterTransactionAssets <- masterTransactionAssets(masterAssetList.map(_.pegHash))
        blockchainAssetList <- blockchainAssetList(allOrderIDs)
        allDocumentsForAllAssets <- allDocumentsForAllAssets(masterTransactionAssets)
      } yield {
        Ok(views.html.component.master.availableAssetListWithLogin(masterTransactionAssets, masterAssetList, blockchainAssetList, allDocumentsForAllAssets))
      }
        ).recover {
        case _: BaseException => NoContent
      }
  }

  def recentActivityForOrganization(pageNumber: Int = 0): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.getID(loginState.username)
      def tradersInOrganizations(organizationID: String): Future[Seq[Trader]] = masterTraders.Service.getTradersListInOrganization(organizationID)
      def notificationsOfTraders(traderAccountIDs: Seq[String]): Future[Seq[Notification]] = masterTransactionNotifications.Service.getTradersNotifications(traderAccountIDs, pageNumber*limit, limit)
      (for {
        organizationID <- organizationID
        tradersInOrganizations <- tradersInOrganizations(organizationID)
        notificationsOfTraders <- notificationsOfTraders(tradersInOrganizations.map(_.accountID))
      } yield Ok(views.html.component.master.recentActivities(notificationsOfTraders, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.recentActivityForOrganization), None))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def recentActivityForTrader(pageNumber: Int = 0): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val notifications = masterTransactionNotifications.Service.get(loginState.username, pageNumber * limit, limit)
      (for {
        notifications <- notifications
      } yield Ok(views.html.component.master.recentActivities(notifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.recentActivityForTrader), None))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def recentActivityForTradeRoom(pageNumber: Int = 0, tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeActivities = masterTransactionTradeActivities.Service.getTradeActivity(tradeRoomID)
      def notifications(ids: Seq[String]): Future[Seq[Notification]] = masterTransactionNotifications.Service.getTradeRoomNotifications(loginState.username, ids,pageNumber * limit, limit)
      (for {
        tradeActivities <- tradeActivities
        notifications <- notifications(tradeActivities.map(_.notificationID))
      } yield Ok(views.html.component.master.recentActivities(notifications, utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.recentActivityForTradeRoom), Option(tradeRoomID)))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }


  def accountComet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(blockchainAccounts.Service.accountCometSource(loginState.username) via Comet.json("parent.accountCometMessage")).as(ContentTypes.HTML))
  }

  def assetComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(blockchainAssets.Service.assetCometSource(loginState.username) via Comet.json("parent.assetCometMessage")).as(ContentTypes.HTML))
  }

  def fiatComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(blockchainFiats.Service.fiatCometSource(loginState.username) via Comet.json("parent.fiatCometMessage")).as(ContentTypes.HTML))
  }

  def negotiationComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(blockchainNegotiations.Service.negotiationCometSource(loginState.username) via Comet.json("parent.negotiationCometMessage")).as(ContentTypes.HTML))
  }

  def orderComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok.chunked(blockchainOrders.Service.orderCometSource(loginState.username) via Comet.json("parent.orderCometMessage")).as(ContentTypes.HTML))
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

  def organizationViewTraderList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationid = masterOrganizations.Service.tryGetID(loginState.username)

      def tradersListInOrganization(organizationid: String): Future[Seq[Trader]] = masterTraders.Service.getTradersListInOrganization(organizationid)

      (for {
        organizationid <- organizationid
        tradersListInOrganization <- tradersListInOrganization(organizationid)
      } yield Ok(views.html.component.master.organizationViewTradersList(tradersListInOrganization))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewTrader(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def verifyOrganizationTrader(organizationID: String): Future[Boolean] = masterTraders.Service.verifyOrganizationTrader(traderID = traderID, organizationID)

      def getViewTraderResult(verifyOrganizationTrader: Boolean): Future[Result] = {
        if (verifyOrganizationTrader) {
          val accountID = masterTraders.Service.getAccountId(traderID)

          def address(accountID: String): Future[String] = masterAccounts.Service.getAddress(accountID)

          def buyNegotiations(address: String): Future[Seq[Negotiation]] = blockchainNegotiations.Service.getNegotiationsForBuyerAddress(address)

          def sellNegotiations(address: String): Future[Seq[Negotiation]] = blockchainNegotiations.Service.getNegotiationsForSellerAddress(address)

          val trader = masterTraders.Service.get(traderID)

          def assets(address: String): Future[Seq[models.blockchain.Asset]] = blockchainAssets.Service.getAssetPegWallet(address)

          def fiats(address: String): Future[Seq[Fiat]] = blockchainFiats.Service.getFiatPegWallet(address)

          def buyOrders(buyNegotiations: Seq[Negotiation]): Future[Seq[Order]] = blockchainOrders.Service.getOrders(buyNegotiations.map(_.id))

          def sellOrders(sellNegotiations: Seq[Negotiation]): Future[Seq[Order]] = blockchainOrders.Service.getOrders(sellNegotiations.map(_.id))

          def traderFeedbackHistories(address: String): Future[Seq[TraderFeedbackHistory]] = blockchainTraderFeedbackHistories.Service.get(address)

          for {
            accountID <- accountID
            address <- address(accountID)
            buyNegotiations <- buyNegotiations(address)
            sellNegotiations <- sellNegotiations(address)
            assets <- assets(address)
            fiats <- fiats(address)
            buyOrders <- buyOrders(buyNegotiations)
            sellOrders <- sellOrders(sellNegotiations)
            traderFeedbackHistories <- traderFeedbackHistories(address)
            trader <- trader
          } yield Ok(views.html.component.master.organizationViewTrader(trader = trader, assets = assets, fiats = fiats, buyNegotiations = buyNegotiations, sellNegotiations = sellNegotiations, buyOrders = buyOrders, sellOrders = sellOrders, traderFeedbackHistories = traderFeedbackHistories))
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        organizationID <- organizationID
        verifyOrganizationTrader <- verifyOrganizationTrader(organizationID)
        result <- getViewTraderResult(verifyOrganizationTrader)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def accountDetails = Action {
    Ok(views.html.component.master.account())
  }

  def transactionDetails = Action {
    Ok(views.html.component.master.transactions())
  }

  def tradeDetails = Action {
    Ok(views.html.component.master.trades())
  }

  def subscriptionsDetails = Action {
    Ok(views.html.component.master.subscriptions())
  }

  def settingsDetails = Action {
    Ok(views.html.component.master.settings())
  }

  def identificationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountKYC = masterAccountKYCs.Service.get(loginState.username, constants.File.IDENTIFICATION)
      val identification = masterIdentifications.Service.getOrNoneByAccountID(loginState.username)
      for {
        accountKYC <- accountKYC
        identification <- identification
      } yield Ok(views.html.component.master.identificationDetails(identification = identification, accountKYC = accountKYC))
  }

  def userViewPendingRequests: Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountStatus: Future[String] = masterAccounts.Service.getStatus(loginState.username)

      def identification(accountID: String): Future[Option[Identification]] = masterIdentifications.Service.getOrNoneByAccountID(accountID)

      def getZoneOrNoneByOrganization(organization: Option[Organization]): Future[Option[Zone]] = if (organization.isDefined) masterZones.Service.getOrNone(organization.get.zoneID) else Future(None)

      def getOrganizationOrNoneByTrader(trader: Option[Trader]): Future[Option[Organization]] = if (trader.isDefined) masterOrganizations.Service.getOrNone(trader.get.organizationID) else Future(None)

      def getTraderKYCsByTrader(trader: Option[Trader]): Future[Seq[TraderKYC]] = if (trader.isDefined) masterTraderKYCs.Service.getAllDocuments(trader.get.id) else Future(Seq[TraderKYC]())

      def getOrganizationKYCsByOrganization(organization: Option[Organization]): Future[Seq[OrganizationKYC]] = if (organization.isDefined) masterOrganizationKYCs.Service.getAllDocuments(organization.get.id) else Future(Seq[OrganizationKYC]())

      def getTraderOrNoneByAccountID(accountID: String): Future[Option[Trader]] = masterTraders.Service.getOrNoneByAccountID(accountID)

      def getOrganizationOrNoneByAccountID(accountID: String): Future[Option[Organization]] = masterOrganizations.Service.getOrNoneByAccountID(accountID)

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
          Future(Ok(views.html.component.master.userViewPendingRequests(identification = identification, accountStatus = accountStatus)))
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

  def traderViewOrganizationDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader: Future[Trader] = masterTraders.Service.getByAccountID(loginState.username)

      def getOrganizationByID(id: String): Future[Organization] = masterOrganizations.Service.get(id)

      (for {
        trader <- trader
        traderOrganization <- getOrganizationByID(trader.organizationID)
      } yield Ok(views.html.component.master.traderViewOrganizationDetails(traderOrganization))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationDetails: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organization: Future[Organization] = masterOrganizations.Service.getByAccountID(loginState.username)

      def getZone(zoneID: String): Future[Zone] = masterZones.Service.get(zoneID)

      def getOrganizationKYCs(id: String): Future[Seq[OrganizationKYC]] = masterOrganizationKYCs.Service.getAllDocuments(id)

      (for {
        organization <- organization
        organizationZone <- getZone(organization.zoneID)
        organizationKYCs <- getOrganizationKYCs(organization.id)
      } yield Ok(views.html.component.master.viewOrganizationDetails(organizationZone = organizationZone, organization = organization, organizationKYCs = organizationKYCs))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderRelationList(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.traderRelationList(acceptedTraderRelationListRoute = utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.acceptedTraderRelationList), pendingTraderRelationListRoute = utilities.String.getJsRouteFunction(routes.javascript.ComponentViewController.pendingTraderRelationList)))
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
        val fromTrader = masterTraders.Service.get(fromID)
        val toTrader = masterTraders.Service.get(toID)

        def getResult(fromTrader: Trader, toTrader: Trader): Future[Result] = {
          def getOrganizationName(organizationID: String): Future[String] = masterOrganizations.Service.getNameByID(organizationID)

          if (loginState.username == fromTrader.accountID) {
            for {
              organizationName <- getOrganizationName(toTrader.organizationID)
            } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = toTrader.accountID, traderName = toTrader.name, organizationName = organizationName))
          } else if (loginState.username == toTrader.accountID) {
            for {
              organizationName <- getOrganizationName(fromTrader.organizationID)
            } yield Ok(views.html.component.master.acceptedTraderRelation(accountID = fromTrader.accountID, traderName = fromTrader.name, organizationName = organizationName))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
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
        val trader = masterTraders.Service.get(toID)

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
        val fromTrader = masterTraders.Service.get(fromID)
        val toTrader = masterTraders.Service.getByAccountID(loginState.username)

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

  def zoneViewOrganizationBankAccountDetail(organizationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.getZoneID(organizationID)
      val zoneID = masterZones.Service.getID(loginState.username)

      def organizationBankAccountDetail(organizationZoneID: String, zoneID: String): Future[OrganizationBankAccountDetail] = if (organizationZoneID == zoneID) masterOrganizationBankAccountDetails.Service.tryGet(organizationID) else throw new BaseException(constants.Response.UNAUTHORIZED)

      (for {
        organizationZoneID <- organizationZoneID
        zoneID <- zoneID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationZoneID = organizationZoneID, zoneID = zoneID)
      } yield Ok(views.html.component.master.zoneViewOrganizationBankAccountDetail(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewOrganizationBankAccountDetail(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.viewOrganizationBankAccountDetail(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def traderViewOrganizationBankAccountDetail(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

      def organizationBankAccountDetail(organizationID: String): Future[Option[OrganizationBankAccountDetail]] = masterOrganizationBankAccountDetails.Service.get(organizationID)

      (for {
        organizationID <- organizationID
        organizationBankAccountDetail <- organizationBankAccountDetail(organizationID)
      } yield Ok(views.html.component.master.traderViewOrganizationBankAccountDetail(organizationBankAccountDetail))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }
}