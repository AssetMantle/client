package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainZones: blockchain.Zones, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemAsset: transactions.RedeemAsset, blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def redeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemAsset(views.companion.master.RedeemAsset.form))
  }

  def redeemAsset: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RedeemAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.redeemAsset(formWithErrors))
        },
        redeemAssetData => {
          try {
            val toAddress = blockchainZones.Service.getAddress(redeemAssetData.zoneID)
            val ticketID: String = if (kafkaEnabled) transactionsRedeemAsset.Service.kafkaPost(transactionsRedeemAsset.Request(from = username, to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionRedeemAssets.Service.addTransaction(from = username, to = toAddress, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionRedeemAssets.Utility.onSuccess(ticketID, transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(from = username, to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionRedeemAssets.Utility.onFailure(ticketID, blockChainException.message)
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

  def blockchainRedeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.redeemAsset(views.companion.blockchain.RedeemAsset.form))
  }

  def blockchainRedeemAsset: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.RedeemAsset
      .form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.redeemAsset(formWithErrors))
      },
      redeemAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsRedeemAsset.Service.kafkaPost(transactionsRedeemAsset.Request(from = redeemAssetData.from, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            blockchainTransactionRedeemAssets.Service.addTransaction(from = redeemAssetData.from, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(from = redeemAssetData.from, to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas))
            blockchainTransactionRedeemAssets.Service.addTransaction(from = redeemAssetData.from, to = redeemAssetData.to, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
