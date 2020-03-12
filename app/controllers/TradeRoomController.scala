package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import models.master.Contact
import models.masterTransaction.EmailOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeRoomController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, emailOTPs: EmailOTPs, masterContacts: master.Contacts, masterTransactionSalesQuotes: masterTransaction.SalesQuotes, masterTransactionTradeRooms: masterTransaction.TradeRooms, masterTransactionTradeTerms: masterTransaction.TradeTerms, withTraderLoginAction: WithTraderLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_TRADE_ROOM

  private implicit val logger: Logger = Logger(this.getClass)

  def tradeRoom(tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      withUsernameToken.Ok(views.html.tradeRoom(tradeRoomID = tradeRoomID))
  }

  def tradeTerms(tradeRoomID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val tradeTerms = masterTransactionTradeTerms.Service.get(tradeRoomID)
      (for {
        tradeTerms <- tradeTerms
      } yield Ok(views.html.component.master.termsView(tradeTerms))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradeFinancials: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeFinancials()))
  }

  def documentList: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeDocumentList()))
  }

  def documentView(fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.tradeDocumentView(fileName, documentType)))
  }

  def recentActivity: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.recentActivity()))
  }

  def chatRoom: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.chatRoom()))
  }

  def updateTermStatus(tradeID: String, element: String, value: Boolean): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val updateStatus = element match {
        case constants.View.DESCRIPTION => masterTransactionTradeTerms.Service.updateAssetDescriptionStatus(tradeID, value)
        case constants.View.QUANTITY => masterTransactionTradeTerms.Service.updateAssetQuantityStatus(tradeID, value)
        case constants.View.CONTRACT_PRICE => masterTransactionTradeTerms.Service.updateAssetPriceStatus(tradeID, value)
        case constants.View.SHIPMENT_PERIOD => masterTransactionTradeTerms.Service.updateShipmentPeriodStatus(tradeID, value)
        case constants.View.LOAD_PORT => masterTransactionTradeTerms.Service.updatePortOfLoadingStatus(tradeID, value)
        case constants.View.DISCHARGE_PORT => masterTransactionTradeTerms.Service.updatePortOfDischargeStatus(tradeID, value)
        case constants.View.ADVANCE_PAYMENT => masterTransactionTradeTerms.Service.updateAdvancePaymentStatus(tradeID, value)
        case constants.View.CREDIT_TERMS => masterTransactionTradeTerms.Service.updateCreditTermsStatus(tradeID, value)
        case constants.View.BILL_OF_EXCHANGE_REQUIRED => masterTransactionTradeTerms.Service.updateBillOfExchangeRequiredStatus(tradeID, value)
        case constants.View.PRIMARY_DOCUMENTS => masterTransactionTradeTerms.Service.updatePrimaryDocumentsStatus(tradeID, value)
      }
      (for {
        _ <- updateStatus
      } yield Ok
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
