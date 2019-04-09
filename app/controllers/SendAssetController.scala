package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SendAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SendAsset
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSendAsset: transactions.SendAsset, sendAssets: SendAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(master.SendAsset.form))
  }

  def sendAsset: Action[AnyContent] = withTraderLoginAction { implicit request =>
    master.SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.sendAsset(formWithErrors))
      },
      sendAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSendAsset.Service.kafkaPost( transactionsSendAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAssetKafka(from = request.session.get(constants.Security.USERNAME).get, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSendAsset.Service.post( transactionsSendAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAsset(from = request.session.get(constants.Security.USERNAME).get, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
    Ok(views.html.component.blockchain.sendAsset(SendAsset.form))
  }

  def blockchainSendAsset: Action[AnyContent] = Action { implicit request =>
    SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendAsset(formWithErrors))
      },
      sendAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSendAsset.Service.kafkaPost( transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAssetKafka(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSendAsset.Service.post( transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAsset(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
