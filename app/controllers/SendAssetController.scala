package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsSendAsset: transactions.SendAsset, blockchainTransactionSendAssets: blockchainTransaction.SendAssets, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_ASSET

  def sendAssetForm(buyerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
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
              entity = blockchainTransaction.SendAsset(from = loginState.address, to = sendAssetData.buyerAddress, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSendAssets.Service.create,
              request = transactionsSendAsset.Request(transactionsSendAsset.BaseRequest(from = loginState.address, gas = sendAssetData.gas.toString), to = sendAssetData.buyerAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, mode = transactionMode),
              action = transactionsSendAsset.Service.post,
              onSuccess = blockchainTransactionSendAssets.Utility.onSuccess,
              onFailure = blockchainTransactionSendAssets.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSendAssets.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
          transactionsSendAsset.Service.post(transactionsSendAsset.Request(transactionsSendAsset.BaseRequest(from = sendAssetData.from, gas = sendAssetData.gas.toString), to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, mode = sendAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))

        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
