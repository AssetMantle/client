package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAssets: blockchain.Assets, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsSendAsset: transactions.SendAsset, blockchainTransactionSendAssets: blockchainTransaction.SendAssets, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_ASSET

  def sendAssetForm(buyerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(buyerAddress = buyerAddress, pegHash = pegHash))
  }

  def sendAsset: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>

    implicit request =>
      views.companion.master.SendAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future{
            BadRequest(views.html.component.master.sendAsset(formWithErrors, formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name)))
          }
        },
        sendAssetData => {
          val transactionProcess=transaction.process[blockchainTransaction.SendAsset, transactionsSendAsset.Request](
            entity = blockchainTransaction.SendAsset(from = loginState.address, to = sendAssetData.buyerAddress, pegHash = sendAssetData.pegHash, gas = sendAssetData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSendAssets.Service.create,
            request = transactionsSendAsset.Request(transactionsSendAsset.BaseReq(from = loginState.address, gas = sendAssetData.gas.toString), to = sendAssetData.buyerAddress, password = sendAssetData.password, pegHash = sendAssetData.pegHash, mode = transactionMode),
            action = transactionsSendAsset.Service.post,
            onSuccess = blockchainTransactionSendAssets.Utility.onSuccess,
            onFailure = blockchainTransactionSendAssets.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSendAssets.Service.updateTransactionHash
          )
          (for{
            _<-transactionProcess
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))
          ).recover{
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
        }
      )
  }

  def blockchainSendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendAsset())
  }

  def blockchainSendAsset: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.sendAsset(formWithErrors))}
      },
      sendAssetData => {

        val post= transactionsSendAsset.Service.post(transactionsSendAsset.Request(transactionsSendAsset.BaseReq(from = sendAssetData.from, gas = sendAssetData.gas.toString), to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, mode = sendAssetData.mode))
        (for{
          _<-post
        }yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
