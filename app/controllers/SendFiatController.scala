package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import models.masterTransaction
import models.master

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                   blockchainTransactionSendFiats: blockchainTransaction.SendFiats,
                                   masterAccounts: master.Accounts,
                                   masterAssets: master.Assets,
                                   masterTraders: master.Traders,
                                   masterNegotiations: master.Negotiations,
                                   masterTransactionSendFiatRequests: masterTransaction.SendFiatRequests,
                                   transactionsSendFiat: transactions.SendFiat,
                                   transaction: utilities.Transaction,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withZoneLoginAction: WithZoneLoginAction,
                                   withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_FIAT

  def sendFiatForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      (for {
        negotiation <- negotiation
      } yield Ok(views.html.component.master.sendFiat(negotiationID = negotiationID, amount = negotiation.price))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sendFiat(formWithErrors, negotiationID = formWithErrors.data(constants.FormField.NEGOTIATION_ID.name), amount = formWithErrors.data(constants.FormField.AMOUNT.name).toInt)))
        },
        sendFiatData => {

          val negotiation = masterNegotiations.Service.tryGet(sendFiatData.negotiationID)
          def sellerAccountID(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetAccountId(sellerTraderID)
          def sellerAddress(sellerAccountID: String): Future[String] = masterAccounts.Service.tryGetAddress(sellerAccountID)
          def assetPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

          def transactionProcess(sellerAddress:String, pegHash: String): Future[String] = transaction.process[blockchainTransaction.SendFiat, transactionsSendFiat.Request](
            entity = blockchainTransaction.SendFiat(from = loginState.address, to = sellerAddress, amount = sendFiatData.amount, pegHash = pegHash, gas = sendFiatData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendFiats.Service.create,
            request = transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = loginState.address, gas = sendFiatData.gas.toString), to = sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = pegHash, mode = transactionMode),
            action = transactionsSendFiat.Service.post,
            onSuccess = blockchainTransactionSendFiats.Utility.onSuccess,
            onFailure = blockchainTransactionSendFiats.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendFiats.Service.updateTransactionHash
          )

          def createFiatRequest(traderID: String, ticketID: String, negotiationID: String) = masterTransactionSendFiatRequests.Service.create(traderID, ticketID, negotiationID, sendFiatData.amount)

          (for {
            negotiation <- negotiation
            sellerAccountID <- sellerAccountID(negotiation.sellerTraderID)
            sellerAddress <- sellerAddress(sellerAccountID)
            assetPegHash <- assetPegHash(negotiation.assetID)
            ticketID <- transactionProcess(sellerAddress, assetPegHash)
            _ <- createFiatRequest(negotiation.buyerTraderID, ticketID, negotiation.id)
            result <- withUsernameToken.Ok(views.html.tradeRoom(sendFiatData.negotiationID, successes = Seq(constants.Response.FIAT_SENT)))
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
          } yield Ok(views.html.transactions(successes = Seq(constants.Response.FIAT_SENT)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.transactions(failures = Seq(baseException.failure)))
          }
        })
  }

  def blockchainSendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat())
  }

  def blockchainSendFiat: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sendFiat(formWithErrors)))
      },
      sendFiatData => {
        val postRequest = transactionsSendFiat.Service.post(transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = sendFiatData.from, gas = sendFiatData.gas.toString), to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = sendFiatData.pegHash, mode = sendFiatData.mode))
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
