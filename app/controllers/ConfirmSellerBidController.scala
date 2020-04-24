package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Negotiation, Negotiations}
import models.masterTransaction.{NegotiationFile}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                           transaction: utilities.Transaction,
                                           blockchainAccounts: blockchain.Accounts,
                                           masterAccounts: master.Accounts,
                                           withTraderLoginAction: WithTraderLoginAction,
                                           transactionsConfirmSellerBid: transactions.ConfirmSellerBid,
                                           masterNegotiations: Negotiations,
                                           masterTraders: master.Traders,
                                           masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                           blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids,
                                           withUsernameToken: WithUsernameToken,
                                           masterAssets: master.Assets
                                          )
                                          (implicit executionContext: ExecutionContext,
                                           configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_SELLER_BID

  def blockchainConfirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid())
  }

  def blockchainConfirmSellerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => (Future(BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors)))),
      confirmSellerBidData => {
        val postRequest = transactionsConfirmSellerBid.Service.post(transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = confirmSellerBidData.from, gas = confirmSellerBidData.gas.toString), to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid.toString, time = confirmSellerBidData.time.toString, pegHash = confirmSellerBidData.pegHash, sellerContractHash = confirmSellerBidData.sellerContractHash, mode = confirmSellerBidData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
