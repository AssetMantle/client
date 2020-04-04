package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.{Message, MessageReceive, MessageReceives, Chat, Chats, EmailOTPs}
import models.{master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.Comet
import play.api.libs.json.{JsString, JsValue, Json, OWrites, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeRoomController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    chatParticipants: Chats,
                                    messages: masterTransaction.Messages,
                                    messageReceives: MessageReceives,
                                    withLoginAction: WithLoginAction,
                                    masterAccounts: master.Accounts, emailOTPs: EmailOTPs, masterContacts: master.Contacts, masterTransactionTradeTerms: masterTransaction.TradeTerms, withTraderLoginAction: WithTraderLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
      } yield Ok(views.html.component.master.tradeTermsView(tradeTerms))
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

  def updateTradeTermStatus: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateTradeTermStatus.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateTradeTermStatus(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name), formWithErrors.data(constants.FormField.TERM_TYPE.name), formWithErrors.data(constants.FormField.STATUS.name).toBoolean)))
        },
        updateTradeTermStatusData => {
          println("Form Working")
          val tradeID = updateTradeTermStatusData.tradeID
          val status = updateTradeTermStatusData.status
          println(tradeID, status, updateTradeTermStatusData.termType)
          val updateStatus = updateTradeTermStatusData.termType match {
            case constants.View.DESCRIPTION => masterTransactionTradeTerms.Service.updateAssetDescriptionStatus(tradeID, status)
            case constants.View.QUANTITY => masterTransactionTradeTerms.Service.updateAssetQuantityStatus(tradeID, status)
            case constants.View.CONTRACT_PRICE => masterTransactionTradeTerms.Service.updateAssetPriceStatus(tradeID, status)
            case constants.View.SHIPMENT_PERIOD => masterTransactionTradeTerms.Service.updateShipmentPeriodStatus(tradeID, status)
            case constants.View.LOAD_PORT => masterTransactionTradeTerms.Service.updatePortOfLoadingStatus(tradeID, status)
            case constants.View.DISCHARGE_PORT => masterTransactionTradeTerms.Service.updatePortOfDischargeStatus(tradeID, status)
            case constants.View.ADVANCE_PAYMENT => masterTransactionTradeTerms.Service.updateAdvancePaymentStatus(tradeID, status)
            case constants.View.CREDIT_TERMS => masterTransactionTradeTerms.Service.updateCreditTermsStatus(tradeID, status)
            case constants.View.BILL_OF_EXCHANGE_REQUIRED => masterTransactionTradeTerms.Service.updateBillOfExchangeRequiredStatus(tradeID, status)
            case constants.View.PRIMARY_DOCUMENTS => masterTransactionTradeTerms.Service.updatePrimaryDocumentsStatus(tradeID, status)
          }

          (for {
            _ <- updateStatus
            result <- withUsernameToken.PartialContent(views.html.component.master.updateTradeTermStatus(tradeID = tradeID, termType = updateTradeTermStatusData.termType, status = status))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        })
  }
}
