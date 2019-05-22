package controllers

import controllers.actions.{WithLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAssets: blockchain.Assets, blockchainAccounts: blockchain.Accounts, withLoginAction: WithLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsReleaseAsset: transactions.ReleaseAsset, blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def releaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(views.companion.master.ReleaseAsset.form))
  }

  def releaseAsset: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.releaseAsset(formWithErrors))
        },
        releaseAssetData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsReleaseAsset.Service.kafkaPost(transactionsReleaseAsset.Request(from = username, to = releaseAssetData.address, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionReleaseAssets.Service.addReleaseAsset(from = username, to = releaseAssetData.address, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionReleaseAssets.Utility.onSuccess(ticketID, transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(from = username, to = releaseAssetData.address, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionReleaseAssets.Utility.onFailure(ticketID, blockChainException.message)
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

  def blockchainReleaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.releaseAsset(views.companion.blockchain.ReleaseAsset.form))
  }

  def blockchainReleaseAsset: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsReleaseAsset.Service.kafkaPost(transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            blockchainTransactionReleaseAssets.Service.addReleaseAsset(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
            blockchainTransactionReleaseAssets.Service.addReleaseAsset(from = releaseAssetData.from, to = releaseAssetData.to, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
