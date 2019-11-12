package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterTraders: master.Traders, blockchainZones: blockchain.Zones, withTraderLoginAction: WithTraderLoginAction, transactionsRedeemAsset: transactions.RedeemAsset, blockchainACLAccounts: blockchain.ACLAccounts, blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_REDEEM_ASSET
  //TODO Shall we fetch username from login state using withTraderLoginAction and also verify pegHash?
  def redeemAssetForm(ownerAddress: String, pegHash: String): Action[AnyContent] = Action.async { implicit request =>

    val account=blockchainACLAccounts.Service.get(ownerAddress)
    for{
      account<-account
    }yield Ok(views.html.component.master.redeemAsset(zoneID = account.zoneID, pegHash = pegHash))

  }

  def redeemAsset: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future{BadRequest(views.html.component.master.redeemAsset(formWithErrors, formWithErrors.data(constants.FormField.ZONE_ID.name), formWithErrors.data(constants.FormField.PEG_HASH.name)))}
        },
        redeemAssetData => {

          val toAddress = blockchainZones.Service.getAddress(redeemAssetData.zoneID)
          def transactionProcess(toAddress:String)=transaction.process[blockchainTransaction.RedeemAsset, transactionsRedeemAsset.Request](
            entity = blockchainTransaction.RedeemAsset(from = loginState.address, to = toAddress, pegHash = redeemAssetData.pegHash, gas = redeemAssetData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionRedeemAssets.Service.create,
            request = transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseReq(from = loginState.address, gas = redeemAssetData.gas.toString), to = toAddress, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, mode = transactionMode),
            action = transactionsRedeemAsset.Service.post,
            onSuccess = blockchainTransactionRedeemAssets.Utility.onSuccess,
            onFailure = blockchainTransactionRedeemAssets.Utility.onFailure,
            updateTransactionHash = blockchainTransactionRedeemAssets.Service.updateTransactionHash
          )
          (for{
            toAddress<-toAddress
            _<-transactionProcess(toAddress)
          }yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
            ).recover{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainRedeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.redeemAsset())
  }

  def blockchainRedeemAsset: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.RedeemAsset
      .form.bindFromRequest().fold(
      formWithErrors => {
        Future{BadRequest(views.html.component.blockchain.redeemAsset(formWithErrors))}
      },
      redeemAssetData => {

        val post=transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseReq(from = redeemAssetData.from, gas = redeemAssetData.gas.toString), to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, mode = redeemAssetData.mode))
        (for{
          _<-post
        }yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
          ).recover{
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
