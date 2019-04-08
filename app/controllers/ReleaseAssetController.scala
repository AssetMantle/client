package controllers

import controllers.actions.{WithLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ReleaseAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ReleaseAsset
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsReleaseAsset: transactions.ReleaseAsset, releaseAssets: ReleaseAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def releaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(master.ReleaseAsset.form))
  }

  def releaseAsset: Action[AnyContent] = withZoneLoginAction { implicit request =>
    master.ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsReleaseAsset.Service.kafkaPost( transactionsReleaseAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAssetKafka(from = request.session.get(constants.Security.USERNAME).get, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsReleaseAsset.Service.post( transactionsReleaseAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAsset(from = request.session.get(constants.Security.USERNAME).get, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainReleaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.releaseAsset(ReleaseAsset.form))
  }

  def blockchainReleaseAsset: Action[AnyContent] = Action { implicit request =>
    ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsReleaseAsset.Service.kafkaPost( transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAssetKafka(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsReleaseAsset.Service.post( transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            releaseAssets.Service.addReleaseAsset(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
