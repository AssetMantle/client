package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Negotiation, Negotiations}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeSellerBidController @Inject()(
                                           messagesControllerComponents: MessagesControllerComponents,
                                           transactionsChangeSellerBid: transactions.ChangeSellerBid,
                                           withoutLoginAction: WithoutLoginAction,
                                           withoutLoginActionAsync: WithoutLoginActionAsync,
                                         )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_SELLER_BID

  def blockchainChangeSellerBidForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid())
  }

  def blockchainChangeSellerBid: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors)))
      },
      changeSellerBidData => {
        val postRequest = transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseReq(from = changeSellerBidData.from, gas = changeSellerBidData.gas), to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid.toString, time = changeSellerBidData.time.toString, pegHash = changeSellerBidData.pegHash, mode = changeSellerBidData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
