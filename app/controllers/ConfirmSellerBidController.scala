package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.{NegotiationFile, NegotiationRequest}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

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

      val id = masterAccounts.Service.getId(buyerAddress)
      def idByPegHashBuyerAccountIDAndSellerAccountID(id: String) = masterTransactionNegotiationRequests.Service.getIDByPegHashBuyerAccountIDAndSellerAccountID(pegHash, id, loginState.username)
      (for {
        id <- id
        idByPegHashBuyerAccountIDAndSellerAccountID <- idByPegHashBuyerAccountIDAndSellerAccountID(id)
      } yield withUsernameToken.Ok(views.html.component.master.confirmSellerBidDetail(views.companion.master.ConfirmSellerBidDetail.form.fill(views.companion.master.ConfirmSellerBidDetail.Data(idByPegHashBuyerAccountIDAndSellerAccountID, buyerAddress, bid, pegHash))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmSellerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmSellerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.confirmSellerBidDetail(formWithErrors))
          }
        },
        confirmSellerBidData => {

          val sellerAccountID = masterTransactionNegotiationRequests.Service.getSellerAccountID(confirmSellerBidData.requestID)
          def getResult(sellerAccountID: String) = {
            if (loginState.username == sellerAccountID) {
              val updateAmount = masterTransactionNegotiationRequests.Service.updateAmountForID(confirmSellerBidData.requestID, confirmSellerBidData.bid)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getOrNone(confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT)
              for {
                _ <- updateAmount
                negotiationFiles <- negotiationFiles
              } yield withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(negotiationFiles, confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT))
            } else Future {
              Ok
            }
          }
          for {
            sellerAccountID <- sellerAccountID
            result <- getResult(sellerAccountID)
          } yield result
        }
      )
  }

  def confirmSellerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(requestID)
      def getResult(negotiation: NegotiationRequest) = {
        if (negotiation.sellerAccountID == loginState.username) {
          val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)
          for {
            confirmBidDocuments <- confirmBidDocuments
          } yield withUsernameToken.Ok(views.html.component.master.confirmSellerBid(views.companion.master.ConfirmBidTransaction.form.fill(views.companion.master.ConfirmBidTransaction.Data(negotiation.id, 0, constants.FormField.GAS.minimumValue, "")), negotiation, confirmBidDocuments))
        } else {
          Future {
            Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
          }
        }
      }

      (for {
        negotiation <- negotiation
        result <- getResult(negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBidTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))
          def getResult(negotiation: NegotiationRequest) = {
            if (negotiation.sellerAccountID == loginState.username) {
              val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)
              for {
                confirmBidDocuments <- confirmBidDocuments
              } yield BadRequest(views.html.component.master.confirmSellerBid(formWithErrors, negotiation, confirmBidDocuments))
            } else {
              Future {
                Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
              }
            }
          }

          (for {
            negotiation <- negotiation
            result <- getResult(negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        confirmBidTransaction => {

          val masterNegotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(confirmBidTransaction.requestID)

          def getResult(masterNegotiation: NegotiationRequest) = {
            if (masterNegotiation.sellerAccountID == loginState.username) {
              val buyerAddress = masterAccounts.Service.getAddress(masterNegotiation.buyerAccountID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransaction.requestID, Seq(constants.File.SELLER_CONTRACT))
              def sellerContractHash(negotiationFiles: Seq[NegotiationFile]) = utilities.FileOperations.combinedHash(negotiationFiles)
              def transactionProcess(buyerAddress: String, sellerContractHash: String) = transaction.process[blockchainTransaction.ConfirmSellerBid, transactionsConfirmSellerBid.Request](
                entity = blockchainTransaction.ConfirmSellerBid(from = loginState.address, to = buyerAddress, bid = masterNegotiation.amount, time = confirmBidTransaction.time, pegHash = masterNegotiation.pegHash, sellerContractHash = sellerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmSellerBids.Service.create,
                request = transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = buyerAddress, password = confirmBidTransaction.password, bid = masterNegotiation.amount.toString, time = confirmBidTransaction.time.toString, pegHash = masterNegotiation.pegHash, sellerContractHash = sellerContractHash, mode = transactionMode),
                action = transactionsConfirmSellerBid.Service.post,
                onSuccess = blockchainTransactionConfirmSellerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmSellerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmSellerBids.Service.updateTransactionHash
              )

              for {
                buyerAddress <- buyerAddress
                negotiationFiles <- negotiationFiles
                _ <- transactionProcess(buyerAddress, sellerContractHash(negotiationFiles))
              } yield {}
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }

          }

          (for {
            masterNegotiation <- masterNegotiation
          } yield getResult(masterNegotiation)
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainConfirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmSellerBid(views.companion.blockchain.ConfirmSellerBid.form))
  }

  def blockchainConfirmSellerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors))
        }
      },
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
