package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Negotiation
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.MicroLong

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainTransactionSendFiats: blockchainTransaction.SendFiats,
                                   masterAssets: master.Assets,
                                   masterAccounts: master.Accounts,
                                   masterTraders: master.Traders,
                                   masterNegotiations: master.Negotiations,
                                   masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                   transactionsSendFiat: transactions.SendFiat,
                                   transaction: utilities.Transaction,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withZoneLoginAction: WithZoneLoginAction,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_FIAT

  def sendFiatForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(negotiationID)
      (for {
        negotiation <- negotiation
        fiatsInOrder <- fiatsInOrder
      } yield Ok(views.html.component.master.sendFiat(negotiationID = negotiationID, amount = (negotiation.price - fiatsInOrder)))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sendFiat(formWithErrors, negotiationID = formWithErrors.data(constants.FormField.NEGOTIATION_ID.name), amount = new MicroLong(formWithErrors.data(constants.FormField.SEND_AMOUNT.name).toDouble))))
        },
        sendFiatData => {
          val negotiation = masterNegotiations.Service.tryGet(sendFiatData.negotiationID)
          val fiatsInOrder = masterTransactionSendFiatRequests.Service.getFiatsInOrder(sendFiatData.negotiationID)
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = sendFiatData.password)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def assetPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

          def createFiatRequest(traderID: String, ticketID: String, negotiationID: String): Future[String] = masterTransactionSendFiatRequests.Service.create(traderID, ticketID, negotiationID, sendFiatData.sendAmount)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, sellerAddress: String, pegHash: String, negotiation: Negotiation): Future[Result] = {
            if (!loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).sendFiat) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (negotiation.status != constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED) throw new BaseException(constants.Response.CONFIRM_TRANSACTION_PENDING)
            else {
              if (validateUsernamePassword) {
                val ticketID = transaction.process[blockchainTransaction.SendFiat, transactionsSendFiat.Request](
                  entity = blockchainTransaction.SendFiat(from = loginState.address, to = sellerAddress, amount = sendFiatData.sendAmount, pegHash = pegHash, gas = sendFiatData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSendFiats.Service.create,
                  request = transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = loginState.address, gas = sendFiatData.gas.toString), to = sellerAddress, password = sendFiatData.password, amount = sendFiatData.sendAmount.microString, pegHash = pegHash, mode = transactionMode),
                  action = transactionsSendFiat.Service.post,
                  onSuccess = blockchainTransactionSendFiats.Utility.onSuccess,
                  onFailure = blockchainTransactionSendFiats.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSendFiats.Service.updateTransactionHash
                )

                for {
                  ticketID <- ticketID
                  _ <- createFiatRequest(negotiation.buyerTraderID, ticketID, negotiation.id)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(sendFiatData.negotiationID, successes = Seq(constants.Response.FIAT_SENT)))
                } yield result
              } else Future(BadRequest(views.html.component.master.sendFiat(views.companion.master.SendFiat.form.fill(sendFiatData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), negotiationID = sendFiatData.negotiationID, amount = sendFiatData.sendAmount)))
            }
          }

          def getResult(fiatsInOrder: MicroLong, negotiation: master.Negotiation, validateUsernamePassword: Boolean): Future[Result] = {
            if (fiatsInOrder.realDouble + sendFiatData.sendAmount.realDouble <= negotiation.price.realDouble + constants.Precision.SEND_FIAT_PRECISION_MARGIN) {
              for {
                sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
                sellerAddress <- getAddress(sellerAccountID)
                assetPegHash <- assetPegHash(negotiation.assetID)
                result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, sellerAddress = sellerAddress, pegHash = assetPegHash, negotiation = negotiation)
              } yield result
            } else {
              Future(BadRequest(views.html.component.master.sendFiat(views.companion.master.SendFiat.form.fill(sendFiatData).withError(constants.FormField.SEND_AMOUNT.name, constants.Response.FIATS_EXCEED_PENDING_AMOUNT.message, negotiation.price.realDouble - fiatsInOrder.realDouble), sendFiatData.negotiationID, sendFiatData.sendAmount)))
            }
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            fiatsInOrder <- fiatsInOrder
            result <- getResult(fiatsInOrder, negotiation, validateUsernamePassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def zoneSendFiatForm(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future(Ok(views.html.component.master.zoneSendFiat(id = id)))
  }

  def zoneSendFiat: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ZoneSendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.zoneSendFiat(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        sendFiatData => {
          val markSent = masterTransactionSendFiatRequests.Service.markSent(sendFiatData.id)
          (for {
            _ <- markSent
          } yield Ok(views.html.transactionsView(successes = Seq(constants.Response.FIAT_SENT)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.transactionsView(failures = Seq(baseException.failure)))
          }
        })
  }

  def blockchainSendFiatForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.sendFiat())
  }

  def blockchainSendFiat: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sendFiat(formWithErrors)))
      },
      sendFiatData => {
        val postRequest = transactionsSendFiat.Service.post(transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = sendFiatData.from, gas = sendFiatData.gas.toString), to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.sendAmount.microString, pegHash = sendFiatData.pegHash, mode = sendFiatData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
