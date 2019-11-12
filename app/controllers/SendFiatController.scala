package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsSendFiat: transactions.SendFiat, blockchainTransactionSendFiats: blockchainTransaction.SendFiats, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_FIAT

  def sendFiatForm(sellerAddress: String, pegHash: String, amount: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendFiat(sellerAddress = sellerAddress, pegHash = pegHash, amount = amount))
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.sendFiat(formWithErrors, sellerAddress = formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), pegHash = formWithErrors.data(constants.FormField.PEG_HASH.name), amount = formWithErrors.data(constants.FormField.AMOUNT.name).toInt))}
        },
        sendFiatData => {
         /* try {
            transaction.process[blockchainTransaction.SendFiat, transactionsSendFiat.Request](
              entity = blockchainTransaction.SendFiat(from = loginState.address, to = sendFiatData.sellerAddress, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSendFiats.Service.create,
              request = transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = loginState.address, gas = sendFiatData.gas.toString), to = sendFiatData.sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = sendFiatData.pegHash, mode = transactionMode),
              action = transactionsSendFiat.Service.post,
              onSuccess = blockchainTransactionSendFiats.Utility.onSuccess,
              onFailure = blockchainTransactionSendFiats.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSendFiats.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }*/
          transaction.process[blockchainTransaction.SendFiat, transactionsSendFiat.Request](
            entity = blockchainTransaction.SendFiat(from = loginState.address, to = sendFiatData.sellerAddress, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendFiats.Service.create,
            request = transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = loginState.address, gas = sendFiatData.gas.toString), to = sendFiatData.sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = sendFiatData.pegHash, mode = transactionMode),
            action = transactionsSendFiat.Service.post,
            onSuccess = blockchainTransactionSendFiats.Utility.onSuccess,
            onFailure = blockchainTransactionSendFiats.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendFiats.Service.updateTransactionHash
          )
          Future{withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))}
            .recover{
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
        }
      )
  }

  def blockchainSendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat())
  }

  def blockchainSendFiat: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.sendFiat(formWithErrors))}
      },
      sendFiatData => {
      /*  try {
          transactionsSendFiat.Service.post(transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = sendFiatData.from, gas = sendFiatData.gas.toString), to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = sendFiatData.pegHash, mode = sendFiatData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }*/
        val post=transactionsSendFiat.Service.post(transactionsSendFiat.Request(transactionsSendFiat.BaseReq(from = sendFiatData.from, gas = sendFiatData.gas.toString), to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount.toString, pegHash = sendFiatData.pegHash, mode = sendFiatData.mode))
        (for{
          _<-post
        }yield Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
