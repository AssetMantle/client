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

  def confirmBuyerBidDetailForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmBuyerBidDetail(views.companion.master.ConfirmBuyerBidDetail.form))
  }

  def confirmBuyerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBuyerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.confirmBuyerBidDetail(formWithErrors)))
        },
        confirmBuyerBidData => {
          val requestID = confirmBuyerBidData.requestID match {
            case Some(id) => id
            case None => utilities.IDGenerator.requestID()
          }
          val id = masterAccounts.Service.getId(confirmBuyerBidData.sellerAddress)

          def negotiationFiles: Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.get(requestID, constants.File.BUYER_CONTRACT)

          for {
            id <- id
            negotiationFiles <- negotiationFiles
            result <- withUsernameToken.PartialContent(views.html.component.master.confirmBuyerBidDocument(negotiationFiles, requestID, constants.File.BUYER_CONTRACT))
          } yield result
        }
      )
  }

  def confirmBuyerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(requestID)

      def getResult(negotiation: Negotiation): Future[Result] = {
        if (negotiation.buyerTraderID == loginState.username) {
          val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(requestID)
          for {
            confirmBidDocuments <- confirmBidDocuments
          } yield Ok(views.html.component.master.confirmBuyerBid(negotiation = negotiation, files = confirmBidDocuments))
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

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBid.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.REQUEST_ID.name))

          def getResult(negotiation: Negotiation): Future[Result] = {
            if (negotiation.buyerTraderID == loginState.username) {
              val confirmBidDocuments = masterTransactionNegotiationFiles.Service.getConfirmBidDocuments(negotiation.id)
              for {
                confirmBidDocuments <- confirmBidDocuments
              } yield BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors, negotiation, confirmBidDocuments))
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
        confirmBidTransaction => {
          val negotiation = masterNegotiations.Service.tryGet(confirmBidTransaction.requestID)

          def getResult(negotiation: Negotiation): Future[Result] = {
            if (negotiation.buyerTraderID == loginState.username) {
              val sellerAddress = masterAccounts.Service.getAddress(negotiation.sellerTraderID)
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getDocuments(confirmBidTransaction.requestID, Seq(constants.File.BUYER_CONTRACT))
              val pegHash = masterAssets.Service.tryGetPegHash(negotiation.assetID)

              def buyerContractHash(negotiationFiles: Seq[NegotiationFile]): String = utilities.FileOperations.combinedHash(negotiationFiles)

              def transactionProcess(sellerAddress: String, buyerContractHash: String, pegHash: String): Future[String] = transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
                entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = sellerAddress, bid = negotiation.price, time = confirmBidTransaction.time, pegHash = pegHash, buyerContractHash = buyerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
                request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = sellerAddress, password = confirmBidTransaction.password, bid = negotiation.price.toString, time = confirmBidTransaction.time.toString, pegHash = pegHash, buyerContractHash = buyerContractHash, mode = transactionMode),
                action = transactionsConfirmBuyerBid.Service.post,
                onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
              )

              for {
                sellerAddress <- sellerAddress
                negotiationFiles <- negotiationFiles
                pegHash <- pegHash
                _ <- transactionProcess(sellerAddress, buyerContractHash(negotiationFiles), pegHash)
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
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
