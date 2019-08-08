package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsSendAsset: transactions.SendAsset, blockchainTransactionSendAssets: blockchainTransaction.SendAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendAssetForm(buyerAddress:String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(views.companion.master.SendAsset.form, buyerAddress, pegHash))
  }

  def sendAsset: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendAsset(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        sendAssetData => {
          try {
            transaction.process[blockchainTransaction.SendAsset, transactionsSendAsset.Request](
              entity = blockchainTransaction.SendAsset(from = loginState.address, to = sendAssetData.buyerAddress, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionSendAssets.Service.create,
              request = transactionsSendAsset.Request(transactionsSendAsset.BaseRequest(from = loginState.address), to = sendAssetData.buyerAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, mode = transactionMode),
              kafkaAction = transactionsSendAsset.Service.kafkaPost,
              blockAction = transactionsSendAsset.Service.blockPost,
              asyncAction = transactionsSendAsset.Service.asyncPost,
              syncAction = transactionsSendAsset.Service.syncPost,
              onSuccess = blockchainTransactionSendAssets.Utility.onSuccess,
              onFailure = blockchainTransactionSendAssets.Utility.onFailure
            )
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
            transactionsSendAsset.Service.kafkaPost(transactionsSendAsset.Request(transactionsSendAsset.BaseRequest(from = sendAssetData.from), to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, mode = transactionMode))
          } else {
            transactionsSendAsset.Service.blockPost(transactionsSendAsset.Request(transactionsSendAsset.BaseRequest(from = sendAssetData.from), to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, mode = transactionMode))
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
