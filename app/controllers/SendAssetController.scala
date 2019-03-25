package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SendAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendAsset
import views.companion.blockchain.SendAsset

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSendAsset: transactions.SendAsset, sendAssets: SendAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSendAsset.Service.kafkaPost( transactionSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAssetKafka(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSendAsset.Service.post( transactionSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
            sendAssets.Service.addSendAsset(from = sendAssetData.from, to = sendAssetData.to, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
