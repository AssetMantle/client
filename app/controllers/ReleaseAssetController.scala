package controllers

import controllers.actions.{WithLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.LoginState

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
          implicit val loginState:LoginState = LoginState(username)
          try {
            val ticketID: String = if (kafkaEnabled) transactionsReleaseAsset.Service.kafkaPost(transactionsReleaseAsset.Request(from = username, to = releaseAssetData.address, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionReleaseAssets.Service.create(from = username, to = releaseAssetData.address, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, null, txHash = null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionReleaseAssets.Utility.onSuccess(ticketID, transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(from = username, to = releaseAssetData.address, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionReleaseAssets.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
            transactionsReleaseAsset.Service.kafkaPost(transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
          } else {
            transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(from = releaseAssetData.from, to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
