package controllers

import controllers.actions.{WithLoginAction, WithUnknownLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction
import models.{blockchain, master}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, withLoginAction: WithLoginAction, withUnknownLoginAction: WithUnknownLoginAction, transactionSendCoin: transactions.SendCoin, blockchainTransactionSendCoins: blockchainTransaction.SendCoins, withUserLoginAction: WithUserLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendCoin(views.companion.master.SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = withUserLoginAction { implicit request =>
    views.companion.master.SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.sendCoin(formWithErrors))
      },
      sendCoinData => {
        try {
          if (kafkaEnabled) {
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = request.session.get(constants.Security.USERNAME).get, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoinKafka(from = request.session.get(constants.Security.USERNAME).get, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = request.session.get(constants.Security.USERNAME).get, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoin(from = request.session.get(constants.Security.USERNAME).get, to = sendCoinData.to, amount = sendCoinData.amount, gas = sendCoinData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoinKafka(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoin(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
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

  def requestCoinsForm: Action[AnyContent] = withUnknownLoginAction { implicit request =>
    Ok(views.html.component.master.requestCoins(views.companion.master.RequestCoins.form))
    //TODO: Add this to to request database where genesis will approve these!
  }

  def requestCoins: Action[AnyContent] = withLoginAction { implicit request => //TODO: withGenesisLoginAction
    views.companion.master.RequestCoins.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.requestCoins(formWithErrors))
      },
      sendCoinData => {
        try {
          if (kafkaEnabled) {
            val address = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
            val response = transactionSendCoin.Service.kafkaPost(transactionSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = sendCoinData.password, to = address, amount = Seq(transactionSendCoin.Amount("comdex", configuration.get[Int]("blockchain.defaultFossitToken").toString)), gas = sendCoinData.gas))
            blockchainTransactionSendCoins.Service.addSendCoinKafka(constants.User.MAIN_ACCOUNT, address, configuration.get[Int]("blockchain.defaultFossitToken"), sendCoinData.gas, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          }
          else {
            val address = masterAccounts.Service.getAddress(request.session.get(constants.Security.USERNAME).get)
            val mainAddress = masterAccounts.Service.getAddress(constants.User.MAIN_ACCOUNT)
            val response = transactionSendCoin.Service.post(transactionSendCoin.Request(from = constants.User.MAIN_ACCOUNT, password = sendCoinData.password, to = address, amount = Seq(transactionSendCoin.Amount("comdex", configuration.get[Int]("blockchain.defaultFossitToken").toString)), gas = sendCoinData.gas))
            blockchainAccounts.Service.updateSequenceAndCoins(address, blockchainAccounts.Service.getSequence(address) + 1, configuration.get[Int]("blockchain.defaultFossitToken"))
            blockchainAccounts.Service.updateSequenceAndCoins(mainAddress, blockchainAccounts.Service.getSequence(mainAddress) + 1, blockchainAccounts.Service.getCoins(mainAddress) - configuration.get[Int]("blockchain.defaultFossitToken"))
            masterAccounts.Service.updateUserType(request.session.get(constants.Security.USERNAME).get, constants.User.USER)
            blockchainTransactionSendCoins.Service.addSendCoin(constants.User.MAIN_ACCOUNT, address, configuration.get[Int]("blockchain.defaultFossitToken"), sendCoinData.gas, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
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
}
