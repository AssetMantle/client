package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Negotiations
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterNegotiations: Negotiations, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeBuyerBid: transactions.ChangeBuyerBid, blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids)(implicit executionContext: ExecutionContext, configuration: Configuration, withUsernameToken: WithUsernameToken) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CHANGE_BUYER_BID

  def changeBuyerBidForm(sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeBuyerBid())
  }

  def changeBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.changeBuyerBid(formWithErrors)))
        },
        changeBuyerBidData => {
          val requestID = changeBuyerBidData.requestID match {
            case Some(id) => id
            case None => utilities.IDGenerator.requestID()
          }
          val transactionProcess = transaction.process[blockchainTransaction.ChangeBuyerBid, transactionsChangeBuyerBid.Request](
            entity = blockchainTransaction.ChangeBuyerBid(from = loginState.address, to = changeBuyerBidData.sellerAddress, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionChangeBuyerBids.Service.create,
            request = transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = loginState.address, gas = changeBuyerBidData.gas.toString), to = changeBuyerBidData.sellerAddress, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid.toString, time = changeBuyerBidData.time.toString, pegHash = changeBuyerBidData.pegHash, mode = transactionMode),
            action = transactionsChangeBuyerBid.Service.post,
            onSuccess = blockchainTransactionChangeBuyerBids.Utility.onSuccess,
            onFailure = blockchainTransactionChangeBuyerBids.Utility.onFailure,
            updateTransactionHash = blockchainTransactionChangeBuyerBids.Service.updateTransactionHash
          )
          val id = masterAccounts.Service.getId(changeBuyerBidData.sellerAddress)

          (for {
            _ <- transactionProcess
            id <- id
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))
          } yield result
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
        Future(BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors)))
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
