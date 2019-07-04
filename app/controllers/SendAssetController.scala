package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
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
class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSendAsset: transactions.SendAsset, blockchainTransactionSendAssets: blockchainTransaction.SendAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendAssetForm(buyerAddress:String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(views.companion.master.SendAsset.form, buyerAddress, pegHash))
  }

  def sendAsset: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SendAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendAsset(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        sendAssetData => {
          implicit val loginStateL:LoginState = LoginState(username)
          try {
            val ticketID: String = if (kafkaEnabled) transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(from = username, to = sendAssetData.buyerAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSendAssets.Service.create(from = username, to = sendAssetData.buyerAddress, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSendAssets.Utility.onSuccess(ticketID, transactionsSendAsset.Service.post(transactionsSendAsset.Request(from = username, to = sendAssetData.buyerAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionSendAssets.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
            transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
          } else {
            transactionsSendAsset.Service.post(transactionsSendAsset.Request(from = sendAssetData.from, to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
