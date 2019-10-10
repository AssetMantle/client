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
class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                          transaction: utilities.Transaction,
                                          blockchainAccounts: blockchain.Accounts,
                                          masterAccounts: master.Accounts,
                                          withTraderLoginAction: WithTraderLoginAction,
                                          transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid,
                                          masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests,
                                          masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                          blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids,
                                          withUsernameToken: WithUsernameToken)
                                         (implicit executionContext: ExecutionContext,
                                          configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_BUYER_BID

  def confirmBuyerBidDetailForm(sellerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.confirmBuyerBidDetail(views.companion.master.ConfirmBuyerBidDetail.form.fill(views.companion.master.ConfirmBuyerBidDetail.Data(masterTransactionNegotiationRequests.Service.getIDByPegHashAndBuyerAccountID(pegHash, loginState.username), sellerAddress, bid, pegHash))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmBuyerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBuyerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBidDetail(formWithErrors))
        },
        confirmBuyerBidData => {
          try {
            val requestID = confirmBuyerBidData.requestID match {
              case Some(id) => id
              case None => utilities.IDGenerator.requestID()
            }
            masterTransactionNegotiationRequests.Service.insertOrUpdate(requestID, loginState.username, masterAccounts.Service.getId(confirmBuyerBidData.sellerAddress), confirmBuyerBidData.pegHash, confirmBuyerBidData.bid)
            withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.BUYER_CONTRACT), requestID, constants.File.BUYER_CONTRACT))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def confirmBuyerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(requestID)
        if (negotiation.buyerAccountID == loginState.username) {
          withUsernameToken.Ok(views.html.component.master.confirmBuyerBid(views.companion.master.ConfirmBidTransaction.form.fill(views.companion.master.ConfirmBidTransaction.Data(negotiation.id,  0, constants.FormField.GAS.minimumValue, "")), negotiation, masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBidTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))
            if (negotiation.buyerAccountID == loginState.username) {
              BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors, negotiation, masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }
          }catch{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        confirmBidTransaction => {
          try {
            val masterNegotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(confirmBidTransaction.requestID)
            if (masterNegotiation.buyerAccountID == loginState.username) {
              val sellerAddress = masterAccounts.Service.getAddress(masterNegotiation.sellerAccountID)
              val buyerContractHash = utilities.FileOperations.combinedHash(masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransaction.requestID, Seq(constants.File.BUYER_CONTRACT)))
              transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
                  entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = sellerAddress, bid = masterNegotiation.amount, time = confirmBidTransaction.time, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
                  request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = sellerAddress, password = confirmBidTransaction.password, bid = masterNegotiation.amount.toString, time = confirmBidTransaction.time.toString, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, mode = transactionMode),
                  action = transactionsConfirmBuyerBid.Service.post,
                  onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
                  onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
                )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainConfirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmBuyerBid())
  }

  def blockchainConfirmBuyerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmBuyerBid(formWithErrors))
      },
      confirmBuyerBidData => {
        try {
          transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = confirmBuyerBidData.from, gas = confirmBuyerBidData.gas.toString), to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = confirmBuyerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
