package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SendCoins
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendCoin
import views.companion.{blockchain, master}

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: Accounts, blockchainAccounts: models.blockchain.Accounts, withLoginAction: WithLoginAction, transactionSendCoin: SendCoin, sendCoins: SendCoins)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin(master.SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = withLoginAction { implicit request =>
    master.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.sendCoin(formWithErrors))
      },
      sendCoinData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = request.session.get(constants.Security.USERNAME).get, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            sendCoins.Service.addSendCoinKafka(from = request.session.get(constants.Security.USERNAME).get, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = request.session.get(constants.Security.USERNAME).get, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            sendCoins.Service.addSendCoin(from = request.session.get(constants.Security.USERNAME).get, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null,txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
    Ok(views.html.component.blockchain.sendCoin(blockchain.SendCoin.form))
  }

  def blockchainSendCoin: Action[AnyContent] = Action { implicit request =>
    blockchain.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendCoin(formWithErrors))
      },
      sendCoinData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            sendCoins.Service.addSendCoinKafka(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            sendCoins.Service.addSendCoin(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
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
    Ok(views.html.component.master.requestCoins(master.RequestCoins.form))
    //TODO: Add this to to request database where genesis will approve these! Check userType.
  }

  def requestCoins: Action[AnyContent] = withLoginAction { implicit request =>
    master.RequestCoins.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.requestCoins(formWithErrors))
      },
      sendCoinData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val address = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = sendCoinData.password, to = address, amount = Seq(transactionSendCoin.Amount("comdex", constants.User.REQUEST_COINS_AMOUNT.toString)), gas = sendCoinData.gas))
            sendCoins.Service.addSendCoinKafka(constants.User.MAIN_ACCOUNT, address, constants.User.REQUEST_COINS_AMOUNT, sendCoinData.gas, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            if (masterAccounts.Service.getUserType(request.session.get(constants.Security.USERNAME).get) == constants.User.UNKNOWN) {
              val address = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
              val mainAddress = masterAccounts.Service.getAddress(constants.User.MAIN_ACCOUNT)
              val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = sendCoinData.password, to = address, amount = Seq(transactionSendCoin.Amount("comdex", constants.User.REQUEST_COINS_AMOUNT.toString)), gas = sendCoinData.gas))
              blockchainAccounts.Service.updateSequenceAndCoins(address, blockchainAccounts.Service.getSequence(address) + 1, constants.User.REQUEST_COINS_AMOUNT)
              blockchainAccounts.Service.updateSequenceAndCoins(mainAddress, blockchainAccounts.Service.getSequence(mainAddress) + 1, blockchainAccounts.Service.getCoins(mainAddress) - constants.User.REQUEST_COINS_AMOUNT)
              masterAccounts.Service.updateUserType(request.session.get(constants.Security.USERNAME).get, constants.User.USER)
              sendCoins.Service.addSendCoin(constants.User.MAIN_ACCOUNT, address, constants.User.REQUEST_COINS_AMOUNT, sendCoinData.gas, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
              Ok(views.html.index(success = response.TxHash))
            } else Ok(views.html.index(failure = "Your Wallet is Activated. Don't try to hack our system."))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}
