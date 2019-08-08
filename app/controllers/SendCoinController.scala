package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, withLoginAction: WithLoginAction, withGenesisLoginAction: WithGenesisLoginAction, blockchainAccounts: blockchain.Accounts, masterTransactionFaucetRequests: masterTransaction.FaucetRequests, withUnknownLoginAction: WithUnknownLoginAction, transactionsSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val defaultFaucetToken = configuration.get[Int]("blockchain.defaultFaucetToken")

  private val mainAddress = masterAccounts.Service.getAddress(constants.User.MAIN_ACCOUNT)

  private val denominationOfGasToken = configuration.get[String]("blockchain.denom.gas")

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin(views.companion.master.SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendCoin(formWithErrors))
        },
        sendCoinData => {
          try {
            transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
              entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
              request = transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = loginState.address), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas, mode = transactionMode),
              kafkaAction = transactionsSendCoin.Service.kafkaPost,
              blockAction = transactionsSendCoin.Service.blockPost,
              asyncAction = transactionsSendCoin.Service.asyncPost,
              syncAction = transactionsSendCoin.Service.syncPost,
              onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
              onFailure = blockchainTransactionSendCoins.Utility.onFailure
            )
            Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def blockchainSendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendCoin(views.companion.blockchain.SendCoin.form))
  }

  def blockchainSendCoin: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendCoin(formWithErrors))
      },
      sendCoinData => {
        try {
          if (kafkaEnabled) {
            transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = sendCoinData.from), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas, mode = transactionMode))
          }
          else {
            transactionsSendCoin.Service.blockPost(transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = sendCoinData.from), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas, mode = transactionMode))
          }
          Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }

  def requestCoinsForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.requestCoin(views.companion.master.RequestCoin.form))
  }

  def requestCoins: Action[AnyContent] = withUnknownLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RequestCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.requestCoin(formWithErrors))
        },
        requestCoinFormData => {
          try {
            masterTransactionFaucetRequests.Service.create(loginState.username, defaultFaucetToken)
            Ok(views.html.index(successes = Seq(constants.Response.COINS_REQUESTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingFaucetRequests(masterTransactionFaucetRequests.Service.getPendingFaucetRequests))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def rejectFaucetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectFaucetRequest(views.companion.master.RejectFaucetRequest.form, requestID))
  }

  def rejectFaucetRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectFaucetRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
        },
        rejectFaucetRequestData => {

          try {
            masterTransactionFaucetRequests.Service.reject(rejectFaucetRequestData.requestID, comment = rejectFaucetRequestData.comment)
            Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def approveFaucetRequestsForm(requestID: String, accountID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.approveFaucetRequests(views.companion.master.ApproveFaucetRequest.form, requestID, accountID))
  }

  def approveFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ApproveFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.approveFaucetRequests(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID)))
        },
        approveFaucetRequestFormData => {

          try {
            if (masterTransactionFaucetRequests.Service.getStatus(approveFaucetRequestFormData.requestID).isEmpty) {
              val toAddress = masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID)
              val ticketID = transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
                entity = blockchainTransaction.SendCoin(from = loginState.address, to = toAddress, amount = defaultFaucetToken, gas = approveFaucetRequestFormData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                request = transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = loginState.address), password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, defaultFaucetToken.toString)), gas = approveFaucetRequestFormData.gas, mode = transactionMode),
                kafkaAction = transactionsSendCoin.Service.kafkaPost,
                blockAction = transactionsSendCoin.Service.blockPost,
                asyncAction = transactionsSendCoin.Service.asyncPost,
                syncAction = transactionsSendCoin.Service.syncPost,
                onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                onFailure = blockchainTransactionSendCoins.Utility.onFailure
              )
              masterTransactionFaucetRequests.Service.accept(approveFaucetRequestFormData.requestID, ticketID, approveFaucetRequestFormData.gas)
              Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_APPROVED)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

}
