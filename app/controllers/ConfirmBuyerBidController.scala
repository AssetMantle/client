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

      val id = masterTransactionNegotiationRequests.Service.getIDByPegHashAndBuyerAccountID(pegHash, loginState.username)
      (for {
        id <- id
      } yield withUsernameToken.Ok(views.html.component.master.confirmBuyerBidDetail(views.companion.master.ConfirmBuyerBidDetail.form.fill(views.companion.master.ConfirmBuyerBidDetail.Data(id, sellerAddress, bid, pegHash))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmBuyerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBuyerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.confirmBuyerBidDetail(formWithErrors))
          }
        },
        confirmBuyerBidData => {

          val requestID = confirmBuyerBidData.requestID match {
            case Some(id) => id
            case None => utilities.IDGenerator.requestID()
          }
          val id = masterAccounts.Service.getId(confirmBuyerBidData.sellerAddress)

          def insertOrUpdate(id: String) = masterTransactionNegotiationRequests.Service.insertOrUpdate(requestID, loginState.username, id, confirmBuyerBidData.pegHash, confirmBuyerBidData.bid)

          def negotiationFiles = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.BUYER_CONTRACT)

          for {
            id <- id
            _ <- insertOrUpdate(id)
            negotiationFiles <- negotiationFiles
          } yield withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(negotiationFiles, requestID, constants.File.BUYER_CONTRACT))

        }
      )
  }

  def confirmBuyerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(requestID)

      def getResult(negotiation: NegotiationRequest) = {
        if (negotiation.buyerAccountID == loginState.username) {
          val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)
          for {
            confirmBidDocuments <- confirmBidDocuments
          } yield withUsernameToken.Ok(views.html.component.master.confirmBuyerBid(views.companion.master.ConfirmBidTransaction.form.fill(views.companion.master.ConfirmBidTransaction.Data(negotiation.id, 0, constants.FormField.GAS.minimumValue, "")), negotiation, confirmBidDocuments))
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

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBidTransaction.form.bindFromRequest().fold(
        formWithErrors => {

          val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))

          def getResult(negotiation: NegotiationRequest) = {
            if (negotiation.buyerAccountID == loginState.username) {
              val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)
              for {
                confirmBidDocuments <- confirmBidDocuments
              } yield BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors, negotiation, confirmBidDocuments))
            } else {
              Future {
                Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
              }
            }
          }

          for {
            negotiation <- negotiation
            result <- getResult(negotiation)
          } yield result
        },
        confirmBidTransaction => {

          val masterNegotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(confirmBidTransaction.requestID)

          def getResult(masterNegotiation: NegotiationRequest) = {
            if (masterNegotiation.buyerAccountID == loginState.username) {
              val sellerAddress = masterAccounts.Service.getAddress(masterNegotiation.sellerAccountID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransaction.requestID, Seq(constants.File.BUYER_CONTRACT))

              def buyerContractHash(negotiationFiles: Seq[NegotiationFile]) = utilities.FileOperations.combinedHash(negotiationFiles)

              def transactionProcess(sellerAddress: String, buyerContractHash: String) = transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
                entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = sellerAddress, bid = masterNegotiation.amount, time = confirmBidTransaction.time, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
                request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = sellerAddress, password = confirmBidTransaction.password, bid = masterNegotiation.amount.toString, time = confirmBidTransaction.time.toString, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, mode = transactionMode),
                action = transactionsConfirmBuyerBid.Service.post,
                onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
              )

              for {
                sellerAddress <- sellerAddress
                negotiationFiles <- negotiationFiles
                _ <- transactionProcess(sellerAddress, buyerContractHash(negotiationFiles))
              } yield {}
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
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

  def blockchainConfirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmBuyerBid())
  }

  def blockchainConfirmBuyerBid: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.confirmBuyerBid(formWithErrors))
        }
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
