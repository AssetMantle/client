package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.blockchain.{ACLAccounts, Negotiation, Order}
import models.masterTransaction.IssueAssetRequest
import models.{blockchain, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTraders: master.Traders, masterAccountKYC: master.AccountKYCs, masterAccountFile: master.AccountFiles, masterZoneKYC: master.ZoneKYCs, masterOrganizationKYCs: master.OrganizationKYCs, masterTraderKYCs: master.TraderKYCs, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, masterTransactionAssetFiles: masterTransaction.AssetFiles, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withOrganizationLoginAction: WithOrganizationLoginAction, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

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

          def zone(zoneID: String) = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield Ok(views.html.component.master.zoneDetails(zone))
        case constants.User.TRADER =>
          val zoneID = masterTraders.Service.getZoneIDByAccountID(loginState.username)

          def zone(zoneID: String) = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield Ok(views.html.component.master.zoneDetails(zone))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }

  }

  def organizationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      (loginState.userType match {
        case constants.User.ORGANIZATION =>
          val organization = masterOrganizations.Service.getByAccountID(loginState.username)
          for {
            organization <- organization
          } yield Ok(views.html.component.master.organizationDetails(organization))
        case constants.User.TRADER =>
          val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

          def organization(organizationID: String) = masterOrganizations.Service.get(organizationID)

          for {
            organizationID <- organizationID
            organization <- organization(organizationID)
          } yield Ok(views.html.component.master.organizationDetails(organization))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def assetList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val issueAssets = masterTransactionIssueAssetRequests.Service.getTraderAssetList(loginState.username)

      def allDocumentsForAllAssets(issueAssets: Seq[IssueAssetRequest]) = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(issueAssets.map(_.id))

      (for {
        issueAssets <- issueAssets
        allDocumentsForAllAssets <- allDocumentsForAllAssets(issueAssets)
      } yield Ok(views.html.component.master.assetList(issueAssets, allDocumentsForAllAssets))
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

      def orders(negotiations: Seq[Negotiation]) = blockchainOrders.Service.getOrders(negotiations.map(_.id))

      def getNegotiationsOfOrders(negotiations: Seq[Negotiation], orders: Seq[Order]): Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)

      def assets(negotiationsOfOrders: Seq[Negotiation]) = blockchainAssets.Service.getByPegHashes(negotiationsOfOrders.map(_.assetPegHash))

      for {
        negotiations <- negotiations
        orders <- orders(negotiations)
        assets <- assets(getNegotiationsOfOrders(negotiations, orders))
      } yield {
        val negotiationsOfOrders = getNegotiationsOfOrders(negotiations, orders)
        Ok(views.html.component.master.orderList(orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.buyerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.sellerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id)))
      }

  }

  def availableAssetList: Action[AnyContent] = Action.async { implicit request =>

    val assets = masterTransactionIssueAssetRequests.Service.getMarketAssets()

    def allDocumentsForAllAssets(assets: Seq[IssueAssetRequest]) = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(assets.map(_.id))

    (for {
      assets <- assets
      allDocumentsForAllAssets <- allDocumentsForAllAssets(assets)
    } yield Ok(views.html.component.master.availableAssetList(assets, allDocumentsForAllAssets))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val masterTransactionAssets = masterTransactionIssueAssetRequests.Service.getMarketAssets()
      val allOrderIDs = blockchainOrders.Service.getAllOrderIds

      def blockchainAssetList(allOrderID2s: Seq[String]) = blockchainAssets.Service.getAllPublic(allOrderID2s)

      def allDocumentsForAllAssets(masterTransactionAssets: Seq[IssueAssetRequest]) = masterTransactionAssetFiles.Service.getAllDocumentsForAllAssets(masterTransactionAssets.map(_.id))

      (for {
        allOrderIDs <- allOrderIDs
        masterTransactionAssets <- masterTransactionAssets
        blockchainAssetList <- blockchainAssetList(allOrderIDs)
        allDocumentsForAllAssets <- allDocumentsForAllAssets(masterTransactionAssets)
      } yield {
        println(allOrderIDs)
        println(masterTransactionAssets.map(_.pegHash.get))
        println(blockchainAssetList.map(_.pegHash))
        println(allDocumentsForAllAssets)
        Ok(views.html.component.master.availableAssetListWithLogin(masterTransactionAssets, blockchainAssetList, allDocumentsForAllAssets))
      }
        ).recover {
        case _: BaseException => {
        println("getiing noContent 2222222222222222")
          NoContent
        }
      }
  }

  def accountComet: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.chunked(blockchainAccounts.Service.accountCometSource(loginState.username) via Comet.json("parent.accountCometMessage")).as(ContentTypes.HTML)
      }
  }

  def assetComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.chunked(blockchainAssets.Service.assetCometSource(loginState.username) via Comet.json("parent.assetCometMessage")).as(ContentTypes.HTML)
      }
  }

  def fiatComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.chunked(blockchainFiats.Service.fiatCometSource(loginState.username) via Comet.json("parent.fiatCometMessage")).as(ContentTypes.HTML)
      }
  }

  def negotiationComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.chunked(blockchainNegotiations.Service.negotiationCometSource(loginState.username) via Comet.json("parent.negotiationCometMessage")).as(ContentTypes.HTML)
      }
  }

  def orderComet: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.chunked(blockchainOrders.Service.orderCometSource(loginState.username) via Comet.json("parent.orderCometMessage")).as(ContentTypes.HTML)
      }
  }

  def profileDocuments(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*   try {
           val documents: Seq[Document[_]] = loginState.userType match {
             case constants.User.ZONE => masterZoneKYC.Service.getAllDocuments(loginState.username)
             case constants.User.ORGANIZATION => masterOrganizationKYCs.Service.getAllDocuments(masterOrganizations.Service.getID(loginState.username))
             case constants.User.TRADER => masterTraderKYCs.Service.getAllDocuments(masterTraders.Service.getID(loginState.username))
             case constants.User.USER => masterAccountKYC.Service.getAllDocuments(loginState.username)
             case _ => masterAccountFile.Service.getAllDocuments(loginState.username)
           }
           withUsernameToken.Ok(views.html.component.master.profileDocuments(documents))
         } catch {
           case _: BaseException => InternalServerError
         }
   */
      val documents: Future[Seq[Document[_]]] = loginState.userType match {
        case constants.User.ZONE => masterZoneKYC.Service.getAllDocuments(loginState.username)
        case constants.User.ORGANIZATION =>
          val id = masterOrganizations.Service.getID(loginState.username)

          def organizationKYCs(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

          for {
            id <- id
            organizationKYCs <- organizationKYCs(id)
          } yield organizationKYCs
        case constants.User.TRADER =>
          val id = masterTraders.Service.getID(loginState.username)

          def traderKYCs(id: String) = masterTraderKYCs.Service.getAllDocuments(id)

          for {
            id <- id
            traderKYCs <- traderKYCs(id)
          } yield traderKYCs
        case constants.User.USER => masterAccountKYC.Service.getAllDocuments(loginState.username)
        case _ => masterAccountFile.Service.getAllDocuments(loginState.username)
      }
      (for {
        documents <- documents
      } yield Ok(views.html.component.master.profileDocuments(documents))
        ).recover {
        case _: BaseException => InternalServerError
      }
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

      def tradersListInOrganization(organizationid: String) = masterTraders.Service.getTradersListInOrganization(organizationid)

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

      def verifyOrganizationTrader(organizationID: String) = masterTraders.Service.verifyOrganizationTrader(traderID = traderID, organizationID)

      def getViewTraderResult(verifyOrganizationTrader: Boolean) = {
        if (verifyOrganizationTrader) {
          val accountID = masterTraders.Service.getAccountId(traderID)

          def address(accountID: String) = masterAccounts.Service.getAddress(accountID)

          def buyNegotiations(address: String) = blockchainNegotiations.Service.getNegotiationsForBuyerAddress(address)

          def sellNegotiations(address: String) = blockchainNegotiations.Service.getNegotiationsForSellerAddress(address)

          val trader = masterTraders.Service.get(traderID)

          def assets(address: String) = blockchainAssets.Service.getAssetPegWallet(address)

          def fiats(address: String) = blockchainFiats.Service.getFiatPegWallet(address)

          def buyOrders(buyNegotiations: Seq[Negotiation]) = blockchainOrders.Service.getOrders(buyNegotiations.map(_.id))

          def sellOrders(sellNegotiations: Seq[Negotiation]) = blockchainOrders.Service.getOrders(sellNegotiations.map(_.id))

          def traderFeedbackHistories(address: String) = blockchainTraderFeedbackHistories.Service.get(address)

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
          Future {
            Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
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
}