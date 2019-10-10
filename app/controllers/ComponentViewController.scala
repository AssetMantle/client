package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import models.blockchain.{ACLAccounts, Negotiation, Order}
import models.{blockchain, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import queries.GetAccount

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComponentViewController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTraders: master.Traders, masterAccountKYC: master.AccountKYCs, masterAccountFile: master.AccountFiles, masterZoneKYC: master.ZoneKYCs, masterOrganizationKYCs: master.OrganizationKYCs, masterTraderKYCs: master.TraderKYCs, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories, withOrganizationLoginAction: WithOrganizationLoginAction, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, masterAccountFiles: master.AccountFiles, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken)(implicit configuration: Configuration, executionContext: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val genesisAccountName: String = configuration.get[String]("blockchain.genesis.accountName")

  def commonHome: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         loginState.userType match {
           case constants.User.UNKNOWN =>
             withUsernameToken.Ok(views.html.component.master.commonHome(profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)))
           case _ =>
             withUsernameToken.Ok(views.html.component.master.commonHome(blockchainAccounts.Service.getCoins(loginState.address), masterAccountFiles.Service.getProfilePicture(loginState.username)))
         }
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/
      (loginState.userType match {
        case constants.User.UNKNOWN =>
          val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
          for {
            profilePicture <- profilePicture
          } yield withUsernameToken.Ok(views.html.component.master.commonHome(profilePicture = profilePicture))
        case _ =>
          val profilePicture = masterAccountFiles.Service.getProfilePicture(loginState.username)
          val coins = blockchainAccounts.Service.getCoins(loginState.address)
          for {
            profilePicture <- profilePicture
            coins <- coins
          } yield withUsernameToken.Ok(views.html.component.master.commonHome(coins, profilePicture))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }

  }

  def genesisDetails: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         withUsernameToken.Ok(views.html.component.master.genesisDetails(masterAccounts.Service.getAddress(genesisAccountName)))
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/

      val address = masterAccounts.Service.getAddress(genesisAccountName)
      (for {
        address <- address
      } yield withUsernameToken.Ok(views.html.component.master.genesisDetails(address))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        loginState.userType match {
          case constants.User.ZONE => withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.getZoneByAccountID(loginState.username)))
          case constants.User.ORGANIZATION => withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.get(masterOrganizations.Service.getZoneIDByAccountID(loginState.username))))
          case constants.User.TRADER => withUsernameToken.Ok(views.html.component.master.zoneDetails(masterZones.Service.get(masterTraders.Service.getZoneIDByAccountID(loginState.username))))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
      (loginState.userType match {
        case constants.User.ZONE =>
          val zone = masterZones.Service.getZoneByAccountID(loginState.username)
          for {
            zone <- zone
          } yield withUsernameToken.Ok(views.html.component.master.zoneDetails(zone))
        case constants.User.ORGANIZATION =>
          val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)

          def zone(zoneID: String) = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield withUsernameToken.Ok(views.html.component.master.zoneDetails(zone))
        case constants.User.TRADER =>
          val zoneID = masterTraders.Service.getZoneIDByAccountID(loginState.username)

          def zone(zoneID: String) = masterZones.Service.get(zoneID)

          for {
            zoneID <- zoneID
            zone <- zone(zoneID)
          } yield withUsernameToken.Ok(views.html.component.master.zoneDetails(zone))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }

  }

  def organizationDetails: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         loginState.userType match {
           case constants.User.ORGANIZATION => withUsernameToken.Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.getByAccountID(loginState.username)))
           case constants.User.TRADER => withUsernameToken.Ok(views.html.component.master.organizationDetails(masterOrganizations.Service.get(masterTraders.Service.getOrganizationIDByAccountID(loginState.username))))
         }
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/

      (loginState.userType match {
        case constants.User.ORGANIZATION =>
          val organization = masterOrganizations.Service.getByAccountID(loginState.username)
          for {
            organization <- organization
          } yield withUsernameToken.Ok(views.html.component.master.organizationDetails(organization))
        case constants.User.TRADER =>
          val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

          def organization(organizationID: String) = masterOrganizations.Service.get(organizationID)

          for {
            organizationID <- organizationID
            organization <- organization(organizationID)
          } yield withUsernameToken.Ok(views.html.component.master.organizationDetails(organization))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def assetList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
     /* try {
        withUsernameToken.Ok(views.html.component.master.assetList(masterTransactionIssueAssetRequests.Service.getIssueAssetsByAccountID(loginState.username)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/

      val issueAssets = masterTransactionIssueAssetRequests.Service.getIssueAssetsByAccountID(loginState.username)
      (for {
        issueAssets <- issueAssets
      } yield withUsernameToken.Ok(views.html.component.master.assetList(issueAssets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def fiatList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         withUsernameToken.Ok(views.html.component.master.fiatList(blockchainFiats.Service.getFiatPegWallet(loginState.address)))
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/

      val fiats = blockchainFiats.Service.getFiatPegWallet(loginState.address)
      (for {
        fiats <- fiats
      } yield withUsernameToken.Ok(views.html.component.master.fiatList(fiats))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         withUsernameToken.Ok(views.html.component.master.buyNegotiationList(blockchainNegotiations.Service.getNegotiationsForBuyerAddress(loginState.address), blockchainAssets.Service.getAssetPegHashes(loginState.address)))
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/
      val negotiationsForBuyerAddress = blockchainNegotiations.Service.getNegotiationsForBuyerAddress(loginState.address)
      val assetPegHashes = blockchainAssets.Service.getAssetPegHashes(loginState.address)
      (for {
        negotiationsForBuyerAddress <- negotiationsForBuyerAddress
        assetPegHashes <- assetPegHashes
      } yield withUsernameToken.Ok(views.html.component.master.buyNegotiationList(negotiationsForBuyerAddress, assetPegHashes))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sellNegotiationList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*  try {
          withUsernameToken.Ok(views.html.component.master.sellNegotiationList(blockchainNegotiations.Service.getNegotiationsForSellerAddress(loginState.address), blockchainAssets.Service.getAssetPegHashes(loginState.address)))
        } catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }*/
      val negotiationsForSellerAddress = blockchainNegotiations.Service.getNegotiationsForSellerAddress(loginState.address)
      val assetPegHashes = blockchainAssets.Service.getAssetPegHashes(loginState.address)
      (for {
        negotiationsForSellerAddress <- negotiationsForSellerAddress
        assetPegHashes <- assetPegHashes
      } yield withUsernameToken.Ok(views.html.component.master.sellNegotiationList(negotiationsForSellerAddress, assetPegHashes))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def orderList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)
         val orders = blockchainOrders.Service.getOrders(negotiations.map(_.id))
         val negotiationsOfOrders: Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)
         val assets = blockchainAssets.Service.getByPegHashes(negotiationsOfOrders.map(_.assetPegHash))
         withUsernameToken.Ok(views.html.component.master.orderList(orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.buyerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
           orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.sellerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
           orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id)))
       } catch {
         case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
       }*/

      val negotiations = blockchainNegotiations.Service.getNegotiationsForAddress(loginState.address)

      def orders(negotiations: Seq[Negotiation]) = blockchainOrders.Service.getOrders(negotiations.map(_.id))

      def negotiationsOfOrders(negotiations: Seq[Negotiation], orders: Seq[Order]): Seq[Negotiation] = negotiations.filter(negotiation => orders.map(_.id) contains negotiation.id)

      def assets(negotiationsOfOrders: Seq[Negotiation]) = blockchainAssets.Service.getByPegHashes(negotiationsOfOrders.map(_.assetPegHash))

      for {
        negotiations <- negotiations
        orders <- orders(negotiations)
        negotiationsOfOrders <- Future {
          negotiationsOfOrders(negotiations, orders)
        }
        assets <- assets(negotiationsOfOrders)
      } yield {
        withUsernameToken.Ok(views.html.component.master.orderList(orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.buyerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if negotiationsOfOrder.sellerAddress == loginState.address && !assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id),
          orders.filter(order => (for (negotiationsOfOrder <- negotiationsOfOrders; if assets.find(asset => asset.pegHash == negotiationsOfOrder.assetPegHash).orNull.moderated) yield negotiationsOfOrder).map(_.id) contains order.id)))
      }

  }

  def availableAssetList: Action[AnyContent] = Action.async { implicit request =>
    /* try {
       Ok(views.html.component.master.availableAssetList(blockchainAssets.Service.getAllPublic(blockchainOrders.Service.getAllOrderIds)))
     } catch {
       case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
     }*/

    val allOrderIDs = blockchainOrders.Service.getAllOrderIds

    def allPublicAssets(allOrderIDs: Seq[String]) = blockchainAssets.Service.getAllPublic(allOrderIDs)

    (for {
      allOrderIDs <- allOrderIDs
      allPublicAssets <- allPublicAssets(allOrderIDs)
    } yield Ok(views.html.component.master.availableAssetList(allPublicAssets))
      ).recover {
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def availableAssetListWithLogin: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*  try {
          withUsernameToken.Ok(views.html.component.master.availableAssetListWithLogin(blockchainAssets.Service.getAllPublic(blockchainOrders.Service.getAllOrderIds)))
        } catch {
          case _: BaseException => NoContent
        }*/

      val allOrderIDs = blockchainOrders.Service.getAllOrderIds

      def availableAssetListWithLogin(allOrderIDs: Seq[String]) = blockchainAssets.Service.getAllPublic(allOrderIDs)

      (for {
        allOrderIDs <- allOrderIDs
        availableAssetListWithLogin <- availableAssetListWithLogin(allOrderIDs)
      } yield withUsernameToken.Ok(views.html.component.master.availableAssetListWithLogin(availableAssetListWithLogin))
        ).recover {
        case _: BaseException => NoContent
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

          def allDocuments(id: String) = masterOrganizationKYCs.Service.getAllDocuments(id)

          for {
            id <- id
            allDocuments <- allDocuments(id)
          } yield allDocuments
        case constants.User.TRADER =>
          val id = masterTraders.Service.getID(loginState.username)

          def allDocuments(id: String) = masterTraderKYCs.Service.getAllDocuments(id)

          for {
            id <- id
            allDocuments <- allDocuments(id)
          } yield allDocuments
        case constants.User.USER => masterAccountKYC.Service.getAllDocuments(loginState.username)
        case _ => masterAccountFile.Service.getAllDocuments(loginState.username)
      }
      (for {
        documents <- documents
      } yield withUsernameToken.Ok(views.html.component.master.profileDocuments(documents))
        ).recover {
        case _: BaseException => InternalServerError
      }
  }

  def profilePicture(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /* try {
         withUsernameToken.Ok(views.html.component.master.profilePicture(masterAccountFile.Service.getProfilePicture(loginState.username)))
       } catch {
         case _: BaseException => InternalServerError(views.html.component.master.profilePicture())
       }
     */
      val profilePicture = masterAccountFile.Service.getProfilePicture(loginState.username)
      (for {
        profilePicture <- profilePicture
      } yield withUsernameToken.Ok(views.html.component.master.profilePicture(profilePicture))
        ).recover {
        case _: BaseException => InternalServerError(views.html.component.master.profilePicture())
      }
  }

  def organizationViewTraderList(): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        withUsernameToken.Ok(views.html.component.master.organizationViewTradersList(masterTraders.Service.getTradersListInOrganization(masterOrganizations.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/
      val id = masterOrganizations.Service.getID(loginState.username)

      def tradersListInOrganization(id: String) = masterTraders.Service.getTradersListInOrganization(id)

      (for {
        id <- id
        tradersListInOrganization <- tradersListInOrganization(id)
      } yield withUsernameToken.Ok(views.html.component.master.organizationViewTradersList(tradersListInOrganization))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationViewTrader(traderID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      /*try {
        if (masterTraders.Service.verifyOrganizationTrader(traderID = traderID, masterOrganizations.Service.getID(loginState.username))) {
          val address = masterAccounts.Service.getAddress(masterTraders.Service.getAccountId(traderID))
          val buyNegotiations = blockchainNegotiations.Service.getNegotiationsForBuyerAddress(address)
          val sellNegotiations = blockchainNegotiations.Service.getNegotiationsForSellerAddress(address)
          withUsernameToken.Ok(views.html.component.master.organizationViewTrader(trader = masterTraders.Service.get(traderID), assets = blockchainAssets.Service.getAssetPegWallet(address), fiats = blockchainFiats.Service.getFiatPegWallet(address), buyNegotiations = buyNegotiations, sellNegotiations = sellNegotiations, buyOrders = blockchainOrders.Service.getOrders(buyNegotiations.map(_.id)), sellOrders = blockchainOrders.Service.getOrders(sellNegotiations.map(_.id)), traderFeedbackHistories = blockchainTraderFeedbackHistories.Service.get(address)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }*/

      val id = masterOrganizations.Service.getID(loginState.username)

      def verifyOrganizationTrader(id: String) = masterTraders.Service.verifyOrganizationTrader(traderID = traderID, id)

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
          } yield withUsernameToken.Ok(views.html.component.master.organizationViewTrader(trader = trader, assets = assets, fiats = fiats, buyNegotiations = buyNegotiations, sellNegotiations = sellNegotiations, buyOrders = buyOrders, sellOrders = sellOrders, traderFeedbackHistories = traderFeedbackHistories))
        } else {
          Future {
            Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
        }
      }

      (for {
        id <- id
        verifyOrganizationTrader <- verifyOrganizationTrader(id)
        result <- getViewTraderResult(verifyOrganizationTrader)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}