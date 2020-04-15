package controllers

import controllers.actions.WithZoneLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.Asset
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                       transaction: utilities.Transaction,
                                       blockchainAssets: blockchain.Assets,
                                       blockchainACLAccounts: blockchain.ACLAccounts,
                                       blockchainZones: blockchain.Zones,
                                       masterAccounts: master.Accounts,
                                       masterZones: master.Zones,
                                       masterTraders: master.Traders,
                                       masterAssets: master.Assets,
                                       withZoneLoginAction: WithZoneLoginAction,
                                       transactionsReleaseAsset: transactions.ReleaseAsset,
                                       blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets,
                                       withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_RELEASE_ASSET

  def releaseAssetForm(assetID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val asset = masterAssets.Service.tryGet(assetID)
      def trader(traderID: String): Future[master.Trader] = masterTraders.Service.tryGet(traderID)
      def getResult(zoneID: String, trader: master.Trader, asset: master.Asset): Future[Result] = {
        if(trader.zoneID == zoneID){
          val blockchainAddress = masterAccounts.Service.getAddress(trader.accountID)
          for {
            blockchainAddress <- blockchainAddress
          } yield Ok(views.html.component.master.releaseAsset(blockchainAddress = blockchainAddress, pegHash = asset.pegHash.getOrElse("")))
      } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }}
      (for{
        zoneID <- zoneID
        asset <- asset
        trader <- trader(asset.ownerID)
        result <- getResult(zoneID, trader, asset)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def releaseAsset(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.FormField.BLOCKCHAIN_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name))))
        },
        releaseAssetData => {
          val transactionProcess = transaction.process[blockchainTransaction.ReleaseAsset, transactionsReleaseAsset.Request](
            entity = blockchainTransaction.ReleaseAsset(from = loginState.address, to = releaseAssetData.blockchainAddress, pegHash = releaseAssetData.pegHash, gas = releaseAssetData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionReleaseAssets.Service.create,
            request = transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = loginState.address, gas = releaseAssetData.gas.toString), to = releaseAssetData.blockchainAddress, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, mode = transactionMode),
            action = transactionsReleaseAsset.Service.post,
            onSuccess = blockchainTransactionReleaseAssets.Utility.onSuccess,
            onFailure = blockchainTransactionReleaseAssets.Utility.onFailure,
            updateTransactionHash = blockchainTransactionReleaseAssets.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  //TODO releaseAsset request, it's getting all locked
  def releaseAssetList(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = blockchainZones.Service.getID(loginState.address)

      def addressesUnderZone(zoneID: String): Future[Seq[String]] = blockchainACLAccounts.Service.getAddressesUnderZone(zoneID)

      def allLockedAssets(addressesUnderZone: Seq[String]): Future[Seq[Asset]] = blockchainAssets.Service.getAllLocked(addressesUnderZone)

      (for {
        zoneID <- zoneID
        addressesUnderZone <- addressesUnderZone(zoneID)
        allLockedAssets <- allLockedAssets(addressesUnderZone)
      } yield Ok(views.html.component.master.releaseAssetList(allLockedAssets))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def blockchainReleaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.releaseAsset())
  }

  def blockchainReleaseAsset: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors)))
      },
      releaseAssetData => {
        val postRequest = transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = releaseAssetData.from, gas = releaseAssetData.gas.toString), to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, mode = releaseAssetData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
