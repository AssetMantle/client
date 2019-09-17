package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, withLoginAction: WithLoginAction, withGenesisLoginAction: WithGenesisLoginAction, blockchainAccounts: blockchain.Accounts, masterTransactionFaucetRequests: masterTransaction.FaucetRequests, withUnknownLoginAction: WithUnknownLoginAction, transactionsSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_COIN

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val defaultFaucetToken = configuration.get[Int]("blockchain.defaultFaucetToken")

  private val denominationOfGasToken = configuration.get[String]("blockchain.denom.gas")

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin(views.companion.master.SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      logger.info(System.currentTimeMillis().toString)
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendCoin(formWithErrors))
        },
        sendCoinData => {
          try {
            transaction.process[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
              entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
              request = transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = loginState.address, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), mode = transactionMode),
              action = transactionsSendCoin.Service.post,
              onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
              onFailure = blockchainTransactionSendCoins.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def sendCoinAsync: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      logger.info(System.currentTimeMillis().toString)
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendCoin(formWithErrors))
        },
        sendCoinData => {
          try {
              transaction.processAsync[blockchainTransaction.SendCoin, transactionsSendCoin.Request](
              entity = blockchainTransaction.SendCoin(from = loginState.address, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSendCoins.Service.createAsync,
              request = transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = loginState.address, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), mode = transactionMode),
              action = transactionsSendCoin.Service.postAsync,
              onSuccess = blockchainTransactionSendCoins.Utility.onSuccessAsync,
              onFailure = blockchainTransactionSendCoins.Utility.onFailureAsync,
              updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHashAsync
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
          transactionsSendCoin.Service.post(transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = sendCoinData.from, gas = sendCoinData.gas.toString), password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), mode = sendCoinData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.COINS_SENT)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.COINS_REQUESTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewPendingFaucetRequests(masterTransactionFaucetRequests.Service.getPendingFaucetRequests))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
                entity = blockchainTransaction.SendCoin(from = loginState.address, to = toAddress, amount = defaultFaucetToken, gas = approveFaucetRequestFormData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionSendCoins.Service.create,
                request = transactionsSendCoin.Request(transactionsSendCoin.BaseRequest(from = loginState.address, gas = approveFaucetRequestFormData.gas.toString), password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, defaultFaucetToken.toString)), mode = transactionMode),
                action = transactionsSendCoin.Service.post,
                onSuccess = blockchainTransactionSendCoins.Utility.onSuccess,
                onFailure = blockchainTransactionSendCoins.Utility.onFailure,
                updateTransactionHash = blockchainTransactionSendCoins.Service.updateTransactionHash
              )
              masterTransactionFaucetRequests.Service.accept(requestID = approveFaucetRequestFormData.requestID, ticketID = ticketID, gas = approveFaucetRequestFormData.gas)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FAUCET_REQUEST_APPROVED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
