package controllers

import controllers.actions.{WithGenesisLoginAction, WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, withLoginAction: WithLoginAction, withGenesisLoginAction: WithGenesisLoginAction, blockchainAccounts: blockchain.Accounts, masterTransactionFaucetRequests: masterTransaction.FaucetRequests, withUnknownLoginAction: WithUnknownLoginAction, transactionsSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val defaultFaucetToken = configuration.get[Int]("blockchain.defaultFaucetToken")

  private val mainAddress = masterAccounts.Service.getAddress(constants.User.MAIN_ACCOUNT)

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
            if (kafkaEnabled) {
              val response = transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = username, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
              blockchainTransactionSendCoins.Service.addSendCoinKafka(from = username, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            }
            else {
              val response = transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = username, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
              blockchainTransactionSendCoins.Service.addSendCoin(from = username, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              blockchainAccounts.Service.updateSequenceAndCoins(sendCoinData.to, blockchainAccounts.Service.getSequence(sendCoinData.to) + 1, defaultFaucetToken)
              blockchainAccounts.Service.updateSequenceAndCoins(mainAddress, blockchainAccounts.Service.getSequence(mainAddress) + 1, blockchainAccounts.Service.getCoins(mainAddress) - defaultFaucetToken)
              Ok(views.html.index(success = response.TxHash))
            }
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
            val response = transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoinKafka(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val response = transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionsSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoin(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, Option(response.TxHash), Random.nextString(32), null)
            Ok(views.html.index(success = response.TxHash))
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

  def requestCoins = withUnknownLoginAction.authenticated { username =>
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
        Ok(views.html.component.master.viewPendingFaucetRequests(masterTransactionFaucetRequests.Service.getPendingFaucetRequests()))
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
            masterTransactionFaucetRequests.Service.updateStatus(rejectFaucetRequestData.requestID, false)
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
              if (kafkaEnabled) {
                val response = transactionsSendCoin.Service.kafkaPost(transactionsSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = approveFaucetRequestFormData.password, to = masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID), amount = Seq(transactionsSendCoin.Amount("comdex", defaultFaucetToken.toString)), gas = approveFaucetRequestFormData.gas))
                blockchainTransactionSendCoins.Service.addSendCoinKafka(constants.User.MAIN_ACCOUNT, masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID), defaultFaucetToken, approveFaucetRequestFormData.gas, null, null, response.ticketID, null)
                masterTransactionFaucetRequests.Service.updateTicketID(approveFaucetRequestFormData.requestID, response.ticketID)
                Ok(views.html.index(success = response.ticketID))
              }
              else {
                val toAddress = masterAccounts.Service.getAddress(approveFaucetRequestFormData.accountID)
                val response = transactionsSendCoin.Service.post(transactionsSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = approveFaucetRequestFormData.password, to = toAddress, amount = Seq(transactionsSendCoin.Amount("comdex", defaultFaucetToken.toString)), gas = approveFaucetRequestFormData.gas))
                masterTransactionFaucetRequests.Service.updateStatusAndGas(approveFaucetRequestFormData.requestID, true, approveFaucetRequestFormData.gas)
                blockchainAccounts.Service.updateSequenceAndCoins(toAddress, blockchainAccounts.Service.getSequence(toAddress) + 1, defaultFaucetToken)
                blockchainAccounts.Service.updateSequenceAndCoins(mainAddress, blockchainAccounts.Service.getSequence(mainAddress) + 1, blockchainAccounts.Service.getCoins(mainAddress) - defaultFaucetToken)
                masterAccounts.Service.updateUserType(approveFaucetRequestFormData.accountID, constants.User.USER)
                blockchainTransactionSendCoins.Service.addSendCoin(constants.User.MAIN_ACCOUNT, toAddress, defaultFaucetToken, approveFaucetRequestFormData.gas, null, Option(response.TxHash), Random.nextString(32), null)
                Ok(views.html.index(success = Messages(constants.Success.APPROVED_FAUCET_REQUEST)))
              }
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
