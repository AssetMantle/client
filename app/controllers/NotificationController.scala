package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Trader
import models.{master, masterTransaction}
import models.masterTransaction.{Notification, Notifications, TradeActivity}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationController @Inject()(
                                        messagesControllerComponents: MessagesControllerComponents,
                                        masterTransactionNotifications: masterTransaction.Notifications,
                                        masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                        withLoginAction: WithLoginAction,
                                        masterOrganizations: master.Organizations,
                                        masterTraders: master.Traders,
                                        masterZones: master.Zones,
                                        masterNegotiations: master.Negotiations,
                                      )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  def recentActivityMessages(pageNumber: Int): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val notifications = masterTransactionNotifications.Service.get(accountID = loginState.username, pageNumber = pageNumber)

      (for {
        notifications <- notifications
      } yield Ok(views.html.component.master.recentActivityMessages(notifications = notifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def tradeActivityMessages(negotiationID: String, pageNumber: Int): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val buyerTraderID = masterNegotiations.Service.tryGetBuyerTraderID(negotiationID)
      val sellerTraderID = masterNegotiations.Service.tryGetSellerTraderID(negotiationID)

      def getOrganizationID(traderID: String): Future[String] = masterTraders.Service.tryGetOrganizationID(traderID)

      def getZoneID(traderID: String): Future[String] = masterTraders.Service.tryGetZoneID(traderID)

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getOrganizationAccountID(organizationID: String): Future[String] = masterOrganizations.Service.tryGetAccountID(organizationID)

      def getZoneAccountID(zoneID: String): Future[String] = masterZones.Service.getAccountId(zoneID)

      def getTradeActivityMessages(accountIDs: String*): Future[Seq[TradeActivity]] = if (accountIDs.contains(loginState.username)) {
        masterTransactionTradeActivities.Service.getAllTradeActivities(negotiationID = negotiationID, pageNumber = pageNumber)
      } else throw new BaseException(constants.Response.UNAUTHORIZED)

      (for {
        buyerTraderID <- buyerTraderID
        sellerTraderID <- sellerTraderID
        buyerOrganizationID <- getOrganizationID(buyerTraderID)
        sellerOrganizationID <- getOrganizationID(sellerTraderID)
        buyerZoneID <- getZoneID(buyerTraderID)
        sellerZoneID <- getZoneID(sellerTraderID)
        buyerAccountID <- getTraderAccountID(buyerTraderID)
        buyerOrganizationAccountID <- getOrganizationAccountID(buyerOrganizationID)
        buyerZoneAccountID <- getZoneAccountID(buyerZoneID)
        sellerAccountID <- getTraderAccountID(sellerTraderID)
        sellerOrganizationAccountID <- getOrganizationAccountID(sellerOrganizationID)
        sellerZoneAccountID <- getZoneAccountID(sellerZoneID)
        tradeActivityMessages <- getTradeActivityMessages(buyerAccountID, buyerOrganizationAccountID, buyerZoneAccountID, sellerAccountID, sellerOrganizationAccountID, sellerZoneAccountID)
      } yield Ok(views.html.component.master.tradeActivityMessages(tradeActivities = tradeActivityMessages))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def unreadNotificationCount(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val unreadNotificationCount = masterTransactionNotifications.Service.getNumberOfUnread(loginState.username)
      (for {
        unreadNotificationCount <- unreadNotificationCount
      } yield Ok(unreadNotificationCount.toString)
        ).recover {
        case _: BaseException => NoContent
      }
  }

  def markNotificationRead(notificationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val markAsRead = masterTransactionNotifications.Service.markAsRead(notificationID)
      val unreadNotificationCount = masterTransactionNotifications.Service.getNumberOfUnread(loginState.username)
      (for {
        _ <- markAsRead
        unreadNotificationCount <- unreadNotificationCount
      } yield Ok(unreadNotificationCount.toString)
        ).recover {
        case _: BaseException => NoContent
      }
  }
}
