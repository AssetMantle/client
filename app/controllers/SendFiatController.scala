package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsSendFiat: transactions.SendFiat, blockchainTransactionSendFiats: blockchainTransaction.SendFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendFiatForm(sellerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendFiat(views.companion.master.SendFiat.form, sellerAddress, pegHash, bid))
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendFiat(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH), formWithErrors.data(constants.Form.BID).toInt))
        },
        sendFiatData => {

          try {
            transaction.process[blockchainTransaction.SendFiat, transactionsSendFiat.Request](
              entity = blockchainTransaction.SendFiat(from = loginState.address, to = sendFiatData.sellerAddress, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionSendFiats.Service.create,
              request = transactionsSendFiat.Request(transactionsSendFiat.BaseRequest(from = loginState.address), to = sendFiatData.sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, mode = transactionMode),
              action = transactionsSendFiat.Service.post,
              onSuccess = blockchainTransactionSendFiats.Utility.onSuccess,
              onFailure = blockchainTransactionSendFiats.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSendFiats.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def blockchainSendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat(views.companion.blockchain.SendFiat.form))
  }

  def blockchainSendFiat: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          transactionsSendFiat.Service.post(transactionsSendFiat.Request(transactionsSendFiat.BaseRequest(from = sendFiatData.from), to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.FIAT_SENT)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
