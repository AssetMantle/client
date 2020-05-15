package controllers

import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Organization, Trader, TraderRelation}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TraderController @Inject()(
                                  utilitiesNotification: utilities.Notification,
                                  withLoginAction: WithLoginAction,
                                  withUsernameToken: WithUsernameToken,
                                  masterOrganizations: master.Organizations,
                                  masterZones: master.Zones,
                                  blockchainAccounts: blockchain.Accounts,
                                  messagesControllerComponents: MessagesControllerComponents,
                                  withTraderLoginAction: WithTraderLoginAction,
                                  withOrganizationLoginAction: WithOrganizationLoginAction,
                                  masterTraderRelations: master.TraderRelations,
                                  masterAccounts: master.Accounts,
                                  masterTraders: master.Traders,
                                  transaction: utilities.Transaction,
                                  blockchainTransactionSetACLs: blockchainTransaction.SetACLs,
                                  transactionsSetACL: transactions.SetACL,
                                  blockchainAclHashes: blockchain.ACLHashes,
                                  withZoneLoginAction: WithZoneLoginAction,
                                )
                                (implicit
                                 executionContext: ExecutionContext,
                                 configuration: Configuration,
                                 wsClient: WSClient,
                                ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_TRADER

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def organizationRejectRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.organizationRejectTraderRequest(views.companion.master.RejectTraderRequest.form.fill(views.companion.master.RejectTraderRequest.Data(traderID = traderID))))
  }

  def organizationRejectRequest: Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.organizationRejectTraderRequest(formWithErrors)))
        },
        organizationRejectRequestData => {
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
          val trader = masterTraders.Service.tryGet(organizationRejectRequestData.traderID)

          def rejectTrader(organizationID: String, trader: Trader): Future[Int] = if (organizationID == trader.organizationID) {
            masterTraders.Service.markRejected(id = organizationRejectRequestData.traderID, comment = organizationRejectRequestData.comment)
          } else throw new BaseException(constants.Response.UNAUTHORIZED)

          (for {
            trader <- trader
            organizationID <- organizationID
            _ <- rejectTrader(organizationID = organizationID, trader = trader)
            _ <- utilitiesNotification.send(trader.accountID, constants.Notification.ORGANIZATION_REJECTED_TRADER_REQUEST, organizationRejectRequestData.comment.getOrElse(constants.View.NO_COMMENTS))
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ORGANIZATION_REJECT_TRADER_REQUEST_SUCCESSFUL)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneRejectRequestForm(traderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.zoneRejectTraderRequest(views.companion.master.RejectTraderRequest.form, traderID))
  }

  def zoneRejectRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectTraderRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.zoneRejectTraderRequest(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name))))
        },
        zoneRejectRequestData => {
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val trader = masterTraders.Service.tryGet(zoneRejectRequestData.traderID)

          def rejectTrader(zoneID: String, trader: Trader): Future[Int] = if (zoneID == trader.zoneID) {
            masterTraders.Service.markRejected(id = zoneRejectRequestData.traderID, comment = zoneRejectRequestData.comment)
          } else throw new BaseException(constants.Response.UNAUTHORIZED)

          (for {
            trader <- trader
            zoneID <- zoneID
            _ <- rejectTrader(zoneID = zoneID, trader = trader)
            _ <- utilitiesNotification.send(trader.accountID, constants.Notification.ZONE_REJECTED_TRADER_REQUEST, zoneRejectRequestData.comment.getOrElse(constants.View.NO_COMMENTS))
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ZONE_REJECT_TRADER_REQUEST_SUCCESSFUL)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
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

          def getOrganization(id: String): Future[Organization] = masterOrganizations.Service.tryGet(id)

          def create(fromTrader: Trader, toTrader: Trader): Future[String] =
            if (fromTrader.organizationID == toTrader.organizationID) throw new BaseException(constants.Response.COUNTERPARTY_TRADER_FROM_SAME_ORGANIZATION)
            else if (toTrader.status.getOrElse(false)) masterTraderRelations.Service.create(fromID = fromTrader.id, toID = toTrader.id)
            else throw new BaseException(constants.Response.UNVERIFIED_TRADER)

          (for {
            fromTrader <- getTrader(loginState.username)
            toTrader <- getTrader(traderRelationRequestData.accountID)
            _ <- create(fromTrader = fromTrader, toTrader)
            fromTraderOrganization <- getOrganization(fromTrader.organizationID)
            toTraderOrganization <- getOrganization(toTrader.organizationID)
            _ <- utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_RELATION_REQUEST_SENT, toTrader.accountID, toTraderOrganization.name)
            _ <- utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_RELATION_REQUEST_SENT, toTrader.accountID, toTraderOrganization.name)
            _ <- utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RELATION_REQUEST_RECEIVED, fromTrader.accountID, fromTraderOrganization.name)
            _ <- utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_RELATION_REQUEST_RECEIVED, fromTrader.accountID, fromTraderOrganization.name)
            result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.TRADER_RELATION_REQUEST_SEND_SUCCESSFUL)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
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
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
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
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
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

          def getOrganization(id: String): Future[Organization] = masterOrganizations.Service.tryGet(id)

          def sendNotificationsAndGetResult(fromTrader: Trader, fromTraderOrganization: Organization, toTrader: Trader, toTraderOrganization: Organization, traderRelation: TraderRelation): Future[Result] = {
            if (acceptOrRejectTraderRelationData.status) {
              for {
                _ <- utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_SENT_RELATION_REQUEST_ACCEPTED, toTrader.accountID, toTraderOrganization.name)
                _ <- utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_SENT_RELATION_REQUEST_ACCEPTED, toTrader.accountID, toTraderOrganization.name)
                _ <- utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED, fromTrader.accountID, fromTraderOrganization.name)
                _ <- utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED, fromTrader.accountID, fromTraderOrganization.name)
              } yield {}
            } else {
              for {
                _ <- utilitiesNotification.send(fromTrader.accountID, constants.Notification.TRADER_SENT_RELATION_REQUEST_REJECTED, toTrader.accountID, toTraderOrganization.name)
                _ <- utilitiesNotification.send(fromTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_SENT_RELATION_REQUEST_REJECTED, toTrader.accountID, toTraderOrganization.name)
                _ <- utilitiesNotification.send(toTrader.accountID, constants.Notification.TRADER_RECEIVED_RELATION_REQUEST_REJECTED, fromTrader.accountID, fromTraderOrganization.name)
                _ <- utilitiesNotification.send(toTraderOrganization.accountID, constants.Notification.ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_REJECTED, fromTrader.accountID, fromTraderOrganization.name)
              } yield {}
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
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
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
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = modifyTraderData.password)
          val trader = masterTraders.Service.tryGetByAccountID(modifyTraderData.accountID)
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def getResult(validateUsernamePassword: Boolean, trader: Trader, organizationID: String): Future[Result] = {
            if (validateUsernamePassword) {
              if (trader.organizationID == organizationID) {
                val zoneID = masterOrganizations.Service.getZoneIDByAccountID(loginState.username)
                val organizationID = masterOrganizations.Service.tryGetID(loginState.username)
                val aclAddress = blockchainAccounts.Service.tryGetAddress(modifyTraderData.accountID)
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
                  result <- withUsernameToken.Ok(views.html.account(successes = Seq(constants.Response.ACL_SET)))
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            } else Future(BadRequest(views.html.component.master.organizationModifyTrader(views.companion.master.ModifyTrader.form.fill(modifyTraderData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), accountID = modifyTraderData.accountID, trader = trader)))
          }

          (for {
            trader <- trader
            validateUsernamePassword <- validateUsernamePassword
            organizationID <- organizationID
            result <- getResult(validateUsernamePassword = validateUsernamePassword, trader = trader, organizationID = organizationID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
