package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeBuyerBid: transactions.ChangeBuyerBid, blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids)(implicit executionContext: ExecutionContext, configuration: Configuration, withUsernameToken: WithUsernameToken) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_BUYER_BID

  def changeBuyerBidForm(sellerAddress: String, pegHash: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByPegHashAndBuyerAccountID(pegHash, loginState.username)
      (for {
        negotiation <- negotiation
      } yield {
        negotiation match {
          case Some(negotiationRequest) => Ok(views.html.component.master.changeBuyerBid(requestID = negotiationRequest.id, sellerAddress = sellerAddress, bid = negotiationRequest.amount, pegHash = pegHash))
          case None => Ok(views.html.component.master.changeBuyerBid(requestID = "", sellerAddress = sellerAddress, bid = 0, pegHash = pegHash))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def changeBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.changeBuyerBid(formWithErrors, requestID = formWithErrors.data(constants.FormField.REQUEST_ID.name), sellerAddress = formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), bid = formWithErrors.data(constants.FormField.BID.name).toInt, pegHash = formWithErrors.data(constants.FormField.PEG_HASH.name)))
          }
        },
        changeBuyerBidData => {
          val requestID = changeBuyerBidData.requestID match {
            case Some(id) => id
            case None => utilities.IDGenerator.requestID()
          }
          val transactionProcess=transaction.process[blockchainTransaction.ChangeBuyerBid, transactionsChangeBuyerBid.Request](
            entity = blockchainTransaction.ChangeBuyerBid(from = loginState.address, to = changeBuyerBidData.sellerAddress, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionChangeBuyerBids.Service.create,
            request = transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = loginState.address, gas = changeBuyerBidData.gas.toString), to = changeBuyerBidData.sellerAddress, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid.toString, time = changeBuyerBidData.time.toString, pegHash = changeBuyerBidData.pegHash, mode = transactionMode),
            action = transactionsChangeBuyerBid.Service.post,
            onSuccess = blockchainTransactionChangeBuyerBids.Utility.onSuccess,
            onFailure = blockchainTransactionChangeBuyerBids.Utility.onFailure,
            updateTransactionHash = blockchainTransactionChangeBuyerBids.Service.updateTransactionHash
          )
          val id = masterAccounts.Service.getId(changeBuyerBidData.sellerAddress)
          def insertOrUpdate(id: String) = masterTransactionNegotiationRequests.Service.insertOrUpdate(requestID, loginState.username, id, changeBuyerBidData.pegHash, changeBuyerBidData.bid)
          (for {
            _<-transactionProcess
            id <- id
            _ <- insertOrUpdate(id)
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainChangeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeBuyerBid())
  }

  def blockchainChangeBuyerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors))
        }
      },
      changeBuyerBidData => {
        val postRequest = transactionsChangeBuyerBid.Service.post(transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = changeBuyerBidData.from, gas = changeBuyerBidData.gas.toString), to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid.toString, time = changeBuyerBidData.time.toString, pegHash = changeBuyerBidData.pegHash, mode = changeBuyerBidData.mode))
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
