package controllers

import controllers.actions.WithZoneLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAssets: blockchain.Assets, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, transactionsReleaseAsset: transactions.ReleaseAsset, blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def releaseAssetForm(ownerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(views.companion.master.ReleaseAsset.form, ownerAddress, pegHash))
  }

  def releaseAsset(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.Form.OWNER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        releaseAssetData => {
          try {
            transaction.process[blockchainTransaction.ReleaseAsset, transactionsReleaseAsset.Request](
              entity = blockchainTransaction.ReleaseAsset(from = loginState.address, to = releaseAssetData.address, pegHash = releaseAssetData.pegHash,gas=releaseAssetData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionReleaseAssets.Service.create,
              request = transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseRequest(from = loginState.address), to = releaseAssetData.address, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash,gas=releaseAssetData.gas.toString, mode = transactionMode),
              action = transactionsReleaseAsset.Service.post,
              onSuccess = blockchainTransactionReleaseAssets.Utility.onSuccess,
              onFailure = blockchainTransactionReleaseAssets.Utility.onFailure,
              updateTransactionHash = blockchainTransactionReleaseAssets.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def releaseAssetList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.releaseAssetList(blockchainAssets.Service.getAllLocked(blockchainACLAccounts.Service.getAddressesUnderZone(blockchainZones.Service.getID(loginState.address)))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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
          transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseRequest(from = releaseAssetData.from), to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash,gas=releaseAssetData.gas.toString, mode = releaseAssetData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
