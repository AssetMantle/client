package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ReleaseAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ReleaseAsset
import views.companion.blockchain.ReleaseAsset

import scala.concurrent.ExecutionContext
import scala.util.Random

class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionReleaseAsset: transactions.ReleaseAsset, releaseAssets: ReleaseAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def releaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.releaseAsset(ReleaseAsset.form))
  }

  def releaseAsset: Action[AnyContent] = Action { implicit request =>
    ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionReleaseAsset.Service.kafkaPost( transactionReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAssetKafka(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionReleaseAsset.Service.post( transactionReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAsset(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
