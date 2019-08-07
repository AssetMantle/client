package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainZones: blockchain.Zones, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemAsset: transactions.RedeemAsset, blockchainACLAccounts: blockchain.ACLAccounts, blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

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
            val ticketID: String = if (kafkaEnabled) transactionsRedeemAsset.Service.kafkaPost(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = loginState.address), to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, mode = transactionMode)).ticketID else Random.nextString(32)
            blockchainTransactionRedeemAssets.Service.create(blockchainTransaction.RedeemAsset(from = loginState.address, to = toAddress, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, status = null, txHash = null, ticketID = ticketID, mode = transactionMode, code = null))
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionRedeemAssets.Utility.onSuccess(ticketID, transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = loginState.address), to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, mode = transactionMode)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionRedeemAssets.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
            transactionsRedeemAsset.Service.kafkaPost(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = redeemAssetData.from), to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, mode = transactionMode))
          } else {
            transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseRequest(from = redeemAssetData.from), to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, mode = transactionMode))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
