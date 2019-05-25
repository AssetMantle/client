package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, withLoginAction: WithLoginAction, withGenesisLoginAction: WithGenesisLoginAction, blockchainAccounts: blockchain.Accounts, masterTransactionFaucetRequests: masterTransaction.FaucetRequests, withUnknownLoginAction: WithUnknownLoginAction, transactionsSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val defaultFaucetToken = configuration.get[Int]("blockchain.defaultFaucetToken")

  private val mainAddress = masterAccounts.Service.getAddress(constants.User.MAIN_ACCOUNT)

  private val denominationOfGasToken = configuration.get[String]("blockchain.denom.gas")

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin(views.companion.master.SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SendCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendCoin(formWithErrors))
        },
        sendCoinData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = username, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSendCoins.Service.addTransaction(from = username, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSendCoins.Utility.onSuccess(ticketID, transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = username, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionSendCoins.Utility.onFailure(ticketID, blockChainException.message)
                }
              }
            }
            Ok(views.html.index(success = ticketID))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
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
            Ok(views.html.index(success = transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas)).ticketID))
          }
          else {
            Ok(views.html.index(success = transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, sendCoinData.amount.toString)), gas = sendCoinData.gas)).TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }

  def requestCoinsForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.requestCoin(views.companion.master.RequestCoin.form))
  }

  def requestCoins: Action[AnyContent] = withUnknownLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RequestCoin.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.requestCoin(formWithErrors))
        },
        requestCoinFormData => {
          try {
            masterTransactionFaucetRequests.Service.addFaucetRequest(username, defaultFaucetToken)
            Ok(views.html.index(success = constants.Success.REQUEST_COINS))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def viewPendingFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingFaucetRequests(masterTransactionFaucetRequests.Service.getPendingFaucetRequests))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def rejectFaucetRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectFaucetRequest(views.companion.master.RejectFaucetRequest.form, requestID))
  }

  def rejectFaucetRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RejectFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectFaucetRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
        },
        rejectFaucetRequestData => {
          try {
            masterTransactionFaucetRequests.Service.reject(rejectFaucetRequestData.requestID, comment = rejectFaucetRequestData.comment)
            Ok(views.html.index(success = Messages(constants.Success.ISSUE_FIAT_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def approveFaucetRequestsForm(requestID: String, accountID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.approveFaucetRequests(views.companion.master.ApproveFaucetRequest.form, requestID, accountID))
  }

  def approveFaucetRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ApproveFaucetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.approveFaucetRequests(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID)))
        },
        approveFaucetRequestFormData => {
          try {
            if (masterTransactionFaucetRequests.Service.getStatus(approveFaucetRequestFormData.requestID).isEmpty) {
              val toAddress = masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID)
              val ticketID: String = if (kafkaEnabled) transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, defaultFaucetToken.toString)), gas = approveFaucetRequestFormData.gas)).ticketID else Random.nextString(32)
              blockchainTransactionSendCoins.Service.addTransaction(constants.User.MAIN_ACCOUNT, toAddress, defaultFaucetToken, approveFaucetRequestFormData.gas, null, null, ticketID, null)
              masterTransactionFaucetRequests.Service.accept(approveFaucetRequestFormData.requestID, ticketID, approveFaucetRequestFormData.gas)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionSendCoins.Utility.onSuccess(ticketID, transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount(denominationOfGasToken, defaultFaucetToken.toString)), gas = approveFaucetRequestFormData.gas)))
                  } catch {
                    case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                      blockchainTransactionSendCoins.Utility.onFailure(ticketID, blockChainException.message)
                  }
                }
              }
              Ok(views.html.index(success = ticketID))
            } else {
              Ok(views.html.index(failure = Messages(constants.Error.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException =>
              masterTransactionFaucetRequests.Service.updateComment(approveFaucetRequestFormData.requestID, blockChainException.message)
              Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }

}
