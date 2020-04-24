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
class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                          transaction: utilities.Transaction,
                                          blockchainAccounts: blockchain.Accounts,
                                          masterAccounts: master.Accounts,
                                          withTraderLoginAction: WithTraderLoginAction,
                                          transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid,
                                          masterNegotiations: Negotiations,
                                          masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                          blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids,
                                          withUsernameToken: WithUsernameToken,
                                          masterAssets: master.Assets,
                                         )
                                         (implicit executionContext: ExecutionContext,
                                          configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_BUYER_BID

  def blockchainConfirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmBuyerBid())
  }

  def blockchainConfirmBuyerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.confirmBuyerBid(formWithErrors)))
      },
      confirmBuyerBidData => {
        val postRequest = transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = confirmBuyerBidData.from, gas = confirmBuyerBidData.gas.toString), to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = confirmBuyerBidData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
