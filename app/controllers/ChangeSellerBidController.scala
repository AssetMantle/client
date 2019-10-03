package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, blockchainTransactionChangeSellerBids: blockchainTransaction.ChangeSellerBids)(implicit executionContext: ExecutionContext, configuration: Configuration, withUsernameToken: WithUsernameToken) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_SELLER_BID

  def changeSellerBidForm(buyerAddress: String, pegHash: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiationRequest = masterTransactionNegotiationRequests.Service.getNegotiationByPegHashBuyerAccountIDAndSellerAccountID(pegHash, masterAccounts.Service.getId(buyerAddress), loginState.username)
        withUsernameToken.Ok(views.html.component.master.changeSellerBid(views.companion.master.ChangeSellerBid.form.fill(views.companion.master.ChangeSellerBid.Data(negotiationRequest.id, "", buyerAddress, negotiationRequest.amount, 0, pegHash, constants.FormField.GAS.minimumValue))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def changeSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeSellerBid(formWithErrors))
        },
        changeSellerBidData => {
          try {
            transaction.process[blockchainTransaction.ChangeSellerBid, transactionsChangeSellerBid.Request](
              entity = blockchainTransaction.ChangeSellerBid(from = loginState.address, to = changeSellerBidData.buyerAddress, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionChangeSellerBids.Service.create,
              request = transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseReq(from = loginState.address, gas = changeSellerBidData.gas.toString), to = changeSellerBidData.buyerAddress, password = changeSellerBidData.password, bid = changeSellerBidData.bid.toString, time = changeSellerBidData.time.toString, pegHash = changeSellerBidData.pegHash, mode = transactionMode),
              action = transactionsChangeSellerBid.Service.post,
              onSuccess = blockchainTransactionChangeSellerBids.Utility.onSuccess,
              onFailure = blockchainTransactionChangeSellerBids.Utility.onFailure,
              updateTransactionHash = blockchainTransactionChangeSellerBids.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainChangeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid())
  }

  def blockchainChangeSellerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseReq(from = changeSellerBidData.from, gas = changeSellerBidData.gas.toString), to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid.toString, time = changeSellerBidData.time.toString, pegHash = changeSellerBidData.pegHash, mode = changeSellerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
