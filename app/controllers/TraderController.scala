package controllers

import actors.ShutdownActor
import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Organization, Trader, TraderRelation}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import services.SFTPScheduler

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TraderController @Inject()(
                                  utilitiesNotification: utilities.Notification,
                                  shutdownActor: ShutdownActor,
                                  withLoginAction: WithLoginAction,
                                  withUsernameToken: WithUsernameToken,
                                  masterOrganizations: master.Organizations,
                                  masterZones: master.Zones,
                                  masterAccounts: master.Accounts,
                                  masterTraderKYCs: master.TraderKYCs,
                                  messagesControllerComponents: MessagesControllerComponents,
                                  withTraderLoginAction: WithTraderLoginAction,
                                  withOrganizationLoginAction: WithOrganizationLoginAction,
                                  masterTraderRelations: master.TraderRelations,
                                  masterTraders: master.Traders,
                                  transaction: utilities.Transaction,
                                  blockchainTransactionSetACLs: blockchainTransaction.SetACLs,
                                  transactionsSetACL: transactions.SetACL,
                                  blockchainAclHashes: blockchain.ACLHashes,
                                )
                                (implicit
                                 executionContext: ExecutionContext,
                                 configuration: Configuration,
                                 wsClient: WSClient,
                                ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_TRADER

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def organizationRejectTraderRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationRejectTraderRequest(views.companion.master.RejectTraderRequest.form.fill(views.companion.master.RejectTraderRequest.Data(traderID = traderID))))
  }

  def organizationRejectTraderRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.organizationRejectTraderRequest(formWithErrors)))
        },
        rejectTraderRequestData => {
          val rejectTrader = masterTraders.Service.rejectTrader(rejectTraderRequestData.traderID)

          (for {
            _ <- rejectTrader
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_REJECT_TRADER_REQUEST_SUCCESSFUL)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.organizationRequest(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def traderRelationRequestForm(): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.traderRelationRequest())
  }

  def traderRelationRequest(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.TraderRelationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.traderRelationRequest(formWithErrors)))
        },
        traderRelationRequestData => {

          def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

          def getOrganization(id: String): Future[Organization] = masterOrganizations.Service.get(id)

          def create(fromTrader: Trader, toTrader: Trader): Future[String] = if (toTrader.verificationStatus.getOrElse(false) && fromTrader.organizationID != toTrader.organizationID) {
            masterTraderRelations.Service.create(fromID = fromTrader.id, toID = toTrader.id)
          } else {
            if (fromTrader.organizationID == toTrader.organizationID) {
              throw new BaseException(constants.Response.COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION)
            }
            throw new BaseException(constants.Response.UNVERIFIED_TRADER)
          }

          def sendNotificationsAndGetResult(fromTrader: Trader, fromTraderOrganization: Organization, toTrader: Trader, toTraderOrganization: Organization): Future[Result] = {
            utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_RELATION_REQUEST_SENT, toTrader.name, toTraderOrganization.name)
            utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_RELATION_REQUEST_SENT, toTrader.name, toTraderOrganization.name)
            utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RELATION_REQUEST_RECEIVED, fromTrader.name, fromTraderOrganization.name)
            utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_RELATION_REQUEST_RECEIVED, fromTrader.name, fromTraderOrganization.name)
            withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.TRADER_RELATION_REQUEST_SEND_SUCCESSFUL)))
          }

          (for {
            fromTrader <- getTrader(loginState.username)
            toTrader <- getTrader(traderRelationRequestData.accountID)
            _ <- create(fromTrader = fromTrader, toTrader)
            fromTraderOrganization <- getOrganization(fromTrader.organizationID)
            toTraderOrganization <- getOrganization(toTrader.organizationID)
            result <- sendNotificationsAndGetResult(fromTrader = fromTrader, fromTraderOrganization = fromTraderOrganization, toTrader = toTrader, toTraderOrganization = toTraderOrganization)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectTraderRelationForm(fromID: String, toID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderRelation = masterTraderRelations.Service.get(fromID = fromID, toID = toID)
      (for {
        traderRelation <- traderRelation
      } yield Ok(views.html.component.master.acceptOrRejectTraderRelation(traderRelation = traderRelation))).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def acceptOrRejectTraderRelation(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrRejectTraderRelation.form.bindFromRequest().fold(
        formWithErrors => {
          val traderRelation = masterTraderRelations.Service.get(fromID = formWithErrors(constants.FormField.FROM.name).value.get, toID = formWithErrors(constants.FormField.TO.name).value.get)
          (for {
            traderRelation <- traderRelation
          } yield BadRequest(views.html.component.master.acceptOrRejectTraderRelation(formWithErrors, traderRelation = traderRelation))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        acceptOrRejectTraderRelationData => {
          val updateStatus: Future[Int] = if (acceptOrRejectTraderRelationData.status) {
            masterTraderRelations.Service.markAccepted(fromID = acceptOrRejectTraderRelationData.fromID, toID = acceptOrRejectTraderRelationData.toID)
          } else {
            masterTraderRelations.Service.markRejected(fromID = acceptOrRejectTraderRelationData.fromID, toID = acceptOrRejectTraderRelationData.toID)
          }

          def traderRelation: Future[TraderRelation] = masterTraderRelations.Service.get(fromID = acceptOrRejectTraderRelationData.fromID, toID = acceptOrRejectTraderRelationData.toID)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getOrganization(id: String): Future[Organization] = masterOrganizations.Service.get(id)

          def sendNotificationsAndGetResult(fromTrader: Trader, fromTraderOrganization: Organization, toTrader: Trader, toTraderOrganization: Organization, traderRelation: TraderRelation): Future[Result] = {
            if (acceptOrRejectTraderRelationData.status) {
              utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_SENT_RELATION_REQUEST_ACCEPTED, toTrader.name, toTraderOrganization.name)
              utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_SENT_RELATION_REQUEST_ACCEPTED, toTrader.name, toTraderOrganization.name)
              utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED, fromTrader.name, fromTraderOrganization.name)
              utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED, fromTrader.name, fromTraderOrganization.name)
            } else {
              utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_SENT_RELATION_REQUEST_REJECTED, toTrader.name, toTraderOrganization.name)
              utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_SENT_RELATION_REQUEST_REJECTED, toTrader.name, toTraderOrganization.name)
              utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RECEIVED_RELATION_REQUEST_REJECTED, fromTrader.name, fromTraderOrganization.name)
              utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_TRADER_RECEIVED_RELATION_REQUEST_REJECTED, fromTrader.name, fromTraderOrganization.name)
            }
            withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectTraderRelation(traderRelation = traderRelation))
          }

          (for {
            _ <- updateStatus
            traderRelation <- traderRelation
            fromTrader <- getTrader(acceptOrRejectTraderRelationData.fromID)
            toTrader <- getTrader(acceptOrRejectTraderRelationData.toID)
            fromTraderOrganization <- getOrganization(fromTrader.organizationID)
            toTraderOrganization <- getOrganization(toTrader.organizationID)
            result <- sendNotificationsAndGetResult(fromTrader = fromTrader, fromTraderOrganization = fromTraderOrganization, toTrader = toTrader, toTraderOrganization = toTraderOrganization, traderRelation = traderRelation)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def organizationModifyTraderForm(accountID: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val trader = masterTraders.Service.tryGetByAccountID(accountID)
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def getResult(trader: Trader, organizationID: String) = if (trader.organizationID == organizationID) {
        Ok(views.html.component.master.organizationModifyTrader(accountID = accountID, trader = trader))
      } else {
        Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
      }

      (for {
        trader <- trader
        organizationID <- organizationID
      } yield getResult(trader, organizationID)).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def organizationModifyTrader: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModifyTrader.form.bindFromRequest().fold(
        formWithErrors => {
          val trader = masterTraders.Service.tryGetByAccountID(formWithErrors.data(constants.FormField.ACCOUNT_ID.name))
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def getResult(trader: Trader, organizationID: String) = if (trader.organizationID == organizationID) {
            BadRequest(views.html.component.master.organizationModifyTrader(formWithErrors, accountID = formWithErrors.data(constants.FormField.ACCOUNT_ID.name), trader = trader))
          } else {
            Unauthorized(views.html.account(failures = Seq(constants.Response.UNAUTHORIZED)))
          }

          (for {
            trader <- trader
            organizationID <- organizationID
          } yield getResult(trader, organizationID)).recover {
            case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        },
        modifyTraderData => {
          val trader = masterTraders.Service.tryGetByAccountID(modifyTraderData.accountID)
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def checkAllKYCFilesVerified(trader: Trader, organizationID: String): Future[Boolean] = if (trader.organizationID == organizationID) {
            masterTraderKYCs.Service.checkAllKYCFilesVerified(trader.id)
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getResult(checkAllKYCFilesVerified: Boolean): Future[Result] = {
            if (checkAllKYCFilesVerified) {
              val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)
              val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
              val aclAddress = masterAccounts.Service.getAddress(modifyTraderData.accountID)
              val acl = blockchain.ACL(issueAsset = modifyTraderData.issueAsset, issueFiat = modifyTraderData.issueFiat, sendAsset = modifyTraderData.sendAsset, sendFiat = modifyTraderData.sendFiat, redeemAsset = modifyTraderData.redeemAsset, redeemFiat = modifyTraderData.redeemFiat, sellerExecuteOrder = modifyTraderData.sellerExecuteOrder, buyerExecuteOrder = modifyTraderData.buyerExecuteOrder, changeBuyerBid = modifyTraderData.changeBuyerBid, changeSellerBid = modifyTraderData.changeSellerBid, confirmBuyerBid = modifyTraderData.confirmBuyerBid, confirmSellerBid = modifyTraderData.changeSellerBid, negotiation = modifyTraderData.negotiation, releaseAsset = modifyTraderData.releaseAsset)
              val createACL = blockchainAclHashes.Service.create(acl)

              def transactionProcess(aclAddress: String, zoneID: String, organizationID: String): Future[String] = transaction.process[blockchainTransaction.SetACL, transactionsSetACL.Request](
                entity = blockchainTransaction.SetACL(from = loginState.address, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, aclHash = util.hashing.MurmurHash3.stringHash(acl.toString).toString, gas = modifyTraderData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSetACLs.Service.create,
                request = transactionsSetACL.Request(transactionsSetACL.BaseReq(from = loginState.address, gas = modifyTraderData.gas.toString), password = modifyTraderData.password, aclAddress = aclAddress, organizationID = organizationID, zoneID = zoneID, issueAsset = modifyTraderData.issueAsset.toString, issueFiat = modifyTraderData.issueFiat.toString, sendAsset = modifyTraderData.sendAsset.toString, sendFiat = modifyTraderData.sendFiat.toString, redeemAsset = modifyTraderData.redeemAsset.toString, redeemFiat = modifyTraderData.redeemFiat.toString, sellerExecuteOrder = modifyTraderData.sellerExecuteOrder.toString, buyerExecuteOrder = modifyTraderData.buyerExecuteOrder.toString, changeBuyerBid = modifyTraderData.changeBuyerBid.toString, changeSellerBid = modifyTraderData.changeSellerBid.toString, confirmBuyerBid = modifyTraderData.confirmBuyerBid.toString, confirmSellerBid = modifyTraderData.confirmSellerBid.toString, negotiation = modifyTraderData.negotiation.toString, releaseAsset = modifyTraderData.releaseAsset.toString, mode = transactionMode),
                action = transactionsSetACL.Service.post,
                onSuccess = blockchainTransactionSetACLs.Utility.onSuccess,
                onFailure = blockchainTransactionSetACLs.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSetACLs.Service.updateTransactionHash
              )

              for {
                aclAddress <- aclAddress
                zoneID <- zoneID
                organizationID <- organizationID
                _ <- createACL
                _ <- transactionProcess(aclAddress, zoneID, organizationID)
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ACL_SET)))
              } yield result
            } else {
              Future(PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_KYC_FILES_NOT_VERIFIED))))
            }
          }

          (for {
            trader <- trader
            organizationID <- organizationID
            checkAllKYCFilesVerified <- checkAllKYCFilesVerified(trader, organizationID)
            result <- getResult(checkAllKYCFilesVerified)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
