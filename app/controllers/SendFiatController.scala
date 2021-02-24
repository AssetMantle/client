package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Negotiation
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.blockchain.SendFiat
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   blockchainAccounts: blockchain.Accounts,
                                   masterAssets: master.Assets,
                                   masterAccounts: master.Accounts,
                                   masterTraders: master.Traders,
                                   masterNegotiations: master.Negotiations,
                                   masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                   transactionsSendFiat: SendFiat,
                                   transaction: utilities.Transaction,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withZoneLoginAction: WithZoneLoginAction,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_FIAT

  def zoneSendFiatForm(id: String): Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneSendFiat(id = id)))
  }

  def zoneSendFiat: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.ZoneSendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.zoneSendFiat(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        sendFiatData => {
          val markSent = masterTransactionSendFiatRequests.Service.markSent(sendFiatData.id)
          (for {
            _ <- markSent
          } yield Ok(views.html.transactionsView(successes = Seq(constants.Response.FIAT_SENT)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.transactionsView(failures = Seq(baseException.failure)))
          }
        })
  }
}
