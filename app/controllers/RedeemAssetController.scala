package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.RedeemAssets
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.RedeemAsset
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionRedeemAsset: transactions.RedeemAsset, redeemAssets: RedeemAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def redeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemAsset(master.RedeemAsset.form))
  }

  def redeemAsset: Action[AnyContent] = withLoginAction { implicit request =>
    master.RedeemAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.redeemAsset(formWithErrors))
      },
      redeemAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionRedeemAsset.Service.kafkaPost( transactionRedeemAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            redeemAssets.Service.addRedeemAssetKafka(from = request.session.get(constants.Security.USERNAME).get, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionRedeemAsset.Service.post( transactionRedeemAsset.Request(from = request.session.get(constants.Security.USERNAME).get, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            redeemAssets.Service.addRedeemAsset(from = request.session.get(constants.Security.USERNAME).get, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainRedeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.redeemAsset(RedeemAsset.form))
  }

  def blockchainRedeemAsset: Action[AnyContent] = Action { implicit request =>
    RedeemAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.redeemAsset(formWithErrors))
      },
      redeemAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionRedeemAsset.Service.kafkaPost( transactionRedeemAsset.Request(from = redeemAssetData.from, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            redeemAssets.Service.addRedeemAssetKafka(from = redeemAssetData.from, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionRedeemAsset.Service.post( transactionRedeemAsset.Request(from = redeemAssetData.from, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            redeemAssets.Service.addRedeemAsset(from = redeemAssetData.from, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
