package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.NegotiationRequest
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, blockchainTransactionChangeSellerBids: blockchainTransaction.ChangeSellerBids)(implicit executionContext: ExecutionContext, configuration: Configuration, withUsernameToken: WithUsernameToken) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_SELLER_BID

  def changeSellerBidForm(buyerAddress: String, pegHash: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterAccounts.Service.getId(buyerAddress)

      def negotiationRequest(id: String): Future[NegotiationRequest] = masterTransactionNegotiationRequests.Service.getNegotiationByPegHashBuyerAccountIDAndSellerAccountID(pegHash, id, loginState.username)

      (for {
        id <- id
        negotiationRequest <- negotiationRequest(id)
      } yield Ok(views.html.component.master.changeSellerBid(requestID = negotiationRequest.id, buyerAddress = buyerAddress, bid = negotiationRequest.amount, pegHash = pegHash))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def changeSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.changeSellerBid(formWithErrors, requestID = formWithErrors.data(constants.FormField.REQUEST_ID.name), buyerAddress = formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), bid = formWithErrors.data(constants.FormField.BID.name).toInt, pegHash = formWithErrors.data(constants.FormField.PEG_HASH.name))))
        },
        changeSellerBidData => {
          val transactionProcess = transaction.process[blockchainTransaction.ChangeSellerBid, transactionsChangeSellerBid.Request](
            entity = blockchainTransaction.ChangeSellerBid(from = loginState.address, to = changeSellerBidData.buyerAddress, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionChangeSellerBids.Service.create,
            request = transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseReq(from = loginState.address, gas = changeSellerBidData.gas.toString), to = changeSellerBidData.buyerAddress, password = changeSellerBidData.password, bid = changeSellerBidData.bid.toString, time = changeSellerBidData.time.toString, pegHash = changeSellerBidData.pegHash, mode = transactionMode),
            action = transactionsChangeSellerBid.Service.post,
            onSuccess = blockchainTransactionChangeSellerBids.Utility.onSuccess,
            onFailure = blockchainTransactionChangeSellerBids.Utility.onFailure,
            updateTransactionHash = blockchainTransactionChangeSellerBids.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainChangeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid())
  }

  def blockchainChangeSellerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors)))
      },
      changeSellerBidData => {
        val postRequest = transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseReq(from = changeSellerBidData.from, gas = changeSellerBidData.gas.toString), to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid.toString, time = changeSellerBidData.time.toString, pegHash = changeSellerBidData.pegHash, mode = changeSellerBidData.mode))
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
