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

  def confirmSellerBidDetailForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmSellerBidDetail(views.companion.master.ConfirmSellerBidDetail.form))
  }

  def confirmSellerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmSellerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.confirmSellerBidDetail(formWithErrors)))
        },
        confirmSellerBidData => {
          val sellerAccountID = masterNegotiations.Service.tryGetNegotiationIDByID(confirmSellerBidData.requestID)

          def updateAmountForIDAndGetResult(sellerAccountID: String): Future[Result] = {
            if (loginState.username == sellerAccountID) {
              val negotiationFiles = masterTransactionNegotiationFiles.Service.get(confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT)
              for {
                negotiationFiles <- negotiationFiles
                result <- withUsernameToken.PartialContent(views.html.component.master.confirmSellerBidDocument(negotiationFiles, confirmSellerBidData.requestID, constants.File.SELLER_CONTRACT))
              } yield result
            } else Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
          }

          for {
            sellerAccountID <- sellerAccountID
            result <- updateAmountForIDAndGetResult(sellerAccountID)
          } yield result
        }
      )
  }

  def confirmSellerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(requestID)

      def getResult(negotiation: Negotiation, traderID: String): Future[Result] = {
        if (negotiation.sellerTraderID == traderID) {
          val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)
          for {
            confirmBidDocuments <- confirmBidDocuments
          } yield Ok(views.html.component.master.confirmSellerBid(negotiation = negotiation, files = confirmBidDocuments))
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(negotiation, traderID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBid.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.REQUEST_ID.name))

          def getResult(negotiation: Negotiation): Future[Result] = {
            if (negotiation.sellerTraderID == loginState.username) {
              val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)
              for {
                confirmBidDocuments <- confirmBidDocuments
              } yield BadRequest(views.html.component.master.confirmSellerBid(formWithErrors, negotiation, confirmBidDocuments))
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
        confirmBidTransactionData => {
          val negotiation = masterNegotiations.Service.tryGet(confirmBidTransactionData.requestID)

          def getResult(negotiation: Negotiation): Future[Result] = {
            if (negotiation.sellerTraderID == loginState.username) {
              val buyerAddress = masterAccounts.Service.getAddress(negotiation.buyerTraderID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransactionData.requestID, Seq(constants.File.SELLER_CONTRACT))
              val pegHash = masterAssets.Service.tryGetPegHash(negotiation.assetID)

              def sellerContractHash(negotiationFiles: Seq[NegotiationFile]): String = utilities.FileOperations.combinedHash(negotiationFiles)

              def transactionProcess(buyerAddress: String, sellerContractHash: String, pegHash: String): Future[String] = transaction.process[blockchainTransaction.ConfirmSellerBid, transactionsConfirmSellerBid.Request](
                entity = blockchainTransaction.ConfirmSellerBid(from = loginState.address, to = buyerAddress, bid = negotiation.price, time = confirmBidTransactionData.time, pegHash = pegHash, sellerContractHash = sellerContractHash, gas = confirmBidTransactionData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmSellerBids.Service.create,
                request = transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = loginState.address, gas = confirmBidTransactionData.gas.toString), to = buyerAddress, password = confirmBidTransactionData.password, bid = negotiation.price.toString, time = confirmBidTransactionData.time.toString, pegHash = pegHash, sellerContractHash = sellerContractHash, mode = transactionMode),
                action = transactionsConfirmSellerBid.Service.post,
                onSuccess = blockchainTransactionConfirmSellerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmSellerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmSellerBids.Service.updateTransactionHash
              )

              for {
                buyerAddress <- buyerAddress
                negotiationFiles <- negotiationFiles
                pegHash <- pegHash
                _ <- transactionProcess(buyerAddress, sellerContractHash(negotiationFiles), pegHash)
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
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
      )
  }

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
