package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.blockchain._
import models.master.{Organization => _, Zone => _, _}
import models.masterTransaction.{AssetFile, IssueAssetRequest}
import models.{blockchain, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTraders: master.Traders, masterAccountKYC: master.AccountKYCs, masterAccountFile: master.AccountFiles, masterZoneKYC: master.ZoneKYCs, masterOrganizationKYCs: master.OrganizationKYCs, masterTraderKYCs: master.TraderKYCs, masterAssets: master.Assets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionAssetFiles: masterTransaction.AssetFiles, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withOrganizationLoginAction: WithOrganizationLoginAction, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

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
      val profilePicture = masterAccountFile.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield Ok(views.html.component.master.profilePicture(profilePicture))
        ).recover {
        case _: BaseException => InternalServerError(views.html.component.master.profilePicture())
      }
  }

  def organizationViewTraderList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationid = masterOrganizations.Service.getID(loginState.username)

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
      val organizationID = masterOrganizations.Service.getID(loginState.username)

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
}