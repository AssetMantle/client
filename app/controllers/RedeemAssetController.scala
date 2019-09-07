package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainZones: blockchain.Zones, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemAsset: transactions.RedeemAsset, blockchainACLAccounts: blockchain.ACLAccounts, blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_REDEEM_ASSET

  def redeemAssetForm(ownerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemAsset(views.companion.master.RedeemAsset.form, blockchainACLAccounts.Service.get(ownerAddress).zoneID, pegHash))
  }

  def redeemAsset: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.redeemAsset(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        redeemAssetData => {
          try {
            val toAddress = blockchainZones.Service.getAddress(redeemAssetData.zoneID)
            transaction.process[blockchainTransaction.RedeemAsset, transactionsRedeemAsset.Request](
              entity = blockchainTransaction.RedeemAsset(from = loginState.address, to = toAddress, pegHash = redeemAssetData.pegHash,gas=redeemAssetData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionRedeemAssets.Service.create,
              request = transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = loginState.address), to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash,gas=redeemAssetData.gas.toString, mode = transactionMode),
              action = transactionsRedeemAsset.Service.post,
              onSuccess = blockchainTransactionRedeemAssets.Utility.onSuccess,
              onFailure = blockchainTransactionRedeemAssets.Utility.onFailure,
              updateTransactionHash = blockchainTransactionRedeemAssets.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
          transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = redeemAssetData.from), to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas=redeemAssetData.gas.toString,mode = redeemAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
