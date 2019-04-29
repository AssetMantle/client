package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchainTransaction, master, blockchain}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import scala.util.control.Breaks._
import scala.concurrent.ExecutionContext
import scala.util.Random

class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSendAsset: transactions.SendAsset, blockchainTransactionSendAssets: blockchainTransaction.SendAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(views.companion.master.SendAsset.form))
  }

  def sendAsset: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SendAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendAsset(formWithErrors))
        },
        sendAssetData => {
          try {
            if (kafkaEnabled) {
              val response = transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(from = username, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
              blockchainTransactionSendAssets.Service.addSendAssetKafka(from = username, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            } else {
              val response = transactionsSendAsset.Service.post(transactionsSendAsset.Request(from = username, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
              val fromAddress = masterAccounts.Service.getAddress(username)
              blockchainTransactionSendAssets.Service.addSendAsset(from = username, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              blockchainAccounts.Service.updateSequence(fromAddress, blockchainAccounts.Service.getSequence(fromAddress) + 1)
              //TODO: InsertOrUpdate Order Table
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

  def blockchainSendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendAsset(views.companion.blockchain.SendAsset.form))
  }

  def blockchainSendAsset: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendAsset(formWithErrors))
      },
      sendAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            blockchainTransactionSendAssets.Service.addSendAssetKafka(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSendAsset.Service.post(transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            blockchainTransactionSendAssets.Service.addSendAsset(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
