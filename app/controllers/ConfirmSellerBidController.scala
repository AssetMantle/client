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
class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                           transaction: utilities.Transaction,
                                           blockchainAccounts: blockchain.Accounts,
                                           masterAccounts: master.Accounts,
                                           withTraderLoginAction: WithTraderLoginAction,
                                           transactionsConfirmSellerBid: transactions.ConfirmSellerBid,
                                           masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests,
                                           masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                           blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids,
                                           withUsernameToken: WithUsernameToken)
                                          (implicit executionContext: ExecutionContext,
                                           configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_SELLER_BID

  def confirmSellerBidDetailForm(buyerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.confirmSellerBidDetail(views.companion.master.ConfirmSellerBidDetail.form.fill(views.companion.master.ConfirmSellerBidDetail.Data(masterTransactionNegotiationRequests.Service.getIDByPegHashBuyerAccountIDAndSellerAccountID(pegHash, masterAccounts.Service.getId(buyerAddress), loginState.username), buyerAddress, bid, pegHash))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmSellerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmSellerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmSellerBidDetail(formWithErrors))
        },
        confirmSellerBidData => {
          try {
            if(loginState.username == masterTransactionNegotiationRequests.Service.getSellerAccountID(confirmSellerBidData.requestID))
            masterTransactionNegotiationRequests.Service.updateAmountForID(confirmSellerBidData.requestID, confirmSellerBidData.bid)
            withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(masterTransactionNegotiationFiles.Service.getOrNone(confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT), confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT))
          } catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def confirmSellerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(requestID)
        if (negotiation.sellerAccountID == loginState.username) {
          withUsernameToken.Ok(views.html.component.master.confirmSellerBid(views.companion.master.ConfirmBidTransaction.form.fill(views.companion.master.ConfirmBidTransaction.Data(negotiation.id,  0, constants.FormField.GAS.minimumValue, "")), negotiation, masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBidTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))
            if (negotiation.sellerAccountID == loginState.username) {
              BadRequest(views.html.component.master.confirmSellerBid(formWithErrors, negotiation, masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)))
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
            if (masterNegotiation.sellerAccountID == loginState.username) {
              val buyerAddress = masterAccounts.Service.getAddress(masterNegotiation.buyerAccountID)
              val sellerContractHash = utilities.FileOperations.combinedHash(masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransaction.requestID, Seq(constants.File.SELLER_CONTRACT)))
              transaction.process[blockchainTransaction.ConfirmSellerBid, transactionsConfirmSellerBid.Request](
                entity = blockchainTransaction.ConfirmSellerBid(from = loginState.address, to = buyerAddress, bid = masterNegotiation.amount, time = confirmBidTransaction.time, pegHash = masterNegotiation.pegHash, sellerContractHash = sellerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmSellerBids.Service.create,
                request = transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = buyerAddress, password = confirmBidTransaction.password, bid = masterNegotiation.amount.toString, time = confirmBidTransaction.time.toString, pegHash = masterNegotiation.pegHash, sellerContractHash = sellerContractHash, mode = transactionMode),
                action = transactionsConfirmSellerBid.Service.post,
                onSuccess = blockchainTransactionConfirmSellerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmSellerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmSellerBids.Service.updateTransactionHash
              )
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
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


  def blockchainConfirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmSellerBid())
  }

  def blockchainConfirmSellerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors))
      },
      confirmSellerBidData => {
        try {
          transactionsConfirmSellerBid.Service.post(transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = confirmSellerBidData.from, gas = confirmSellerBidData.gas.toString), to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid.toString, time = confirmSellerBidData.time.toString, pegHash = confirmSellerBidData.pegHash, sellerContractHash = confirmSellerBidData.sellerContractHash, mode = confirmSellerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
