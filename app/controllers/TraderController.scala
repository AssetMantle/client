package controllers

import actors.ShutdownActor
import controllers.actions.{WithLoginAction, WithOrganizationLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Organization, Trader, TraderRelation}
import models.{blockchain, master, masterTransaction}
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
                                )
                                (implicit
                                 executionContext: ExecutionContext,
                                 configuration: Configuration,
                                 wsClient: WSClient,
                                ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_TRADER

  private implicit val logger: Logger = Logger(this.getClass)

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

          def getTrader(accountID: String): Future[Trader] = masterTraders.Service.getByAccountID(accountID)

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

          def getTrader(accountID: String): Future[Trader] = masterTraders.Service.getByAccountID(accountID)

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

}
