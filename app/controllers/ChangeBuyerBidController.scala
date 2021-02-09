package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Negotiations
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.blockchain.ChangeBuyerBid

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsChangeBuyerBid: ChangeBuyerBid, withoutLoginAction: WithoutLoginAction, withoutLoginActionAsync: WithoutLoginActionAsync)(implicit executionContext: ExecutionContext, configuration: Configuration, withUsernameToken: WithUsernameToken) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_BUYER_BID

  def blockchainChangeBuyerBidForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.changeBuyerBid())
  }

  def blockchainChangeBuyerBid: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors)))
      },
      changeBuyerBidData => {
        val postRequest = transactionsChangeBuyerBid.Service.post(transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = changeBuyerBidData.from, gas = changeBuyerBidData.gas), to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid.toString, time = changeBuyerBidData.time.toString, pegHash = changeBuyerBidData.pegHash, mode = changeBuyerBidData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
