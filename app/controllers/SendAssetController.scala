package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
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
            val toAddress = masterAccounts.Service.getAddress(sendAssetData.accountID)
            val ticketID: String = if (kafkaEnabled) transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(from = username, to = toAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSendAssets.Service.addSendAsset(from = username, to = toAddress, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionSendAssets.Utility.onSuccess(ticketID, transactionsSendAsset.Service.post(transactionsSendAsset.Request(from = username, to = toAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionSendAssets.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionSendAssets.Utility.onFailure(ticketID, blockChainException.message)
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
            blockchainTransactionSendAssets.Service.addSendAsset(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
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
