package controllers

import controllers.actions.WithZoneLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAssets: blockchain.Assets, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, transactionsReleaseAsset: transactions.ReleaseAsset, blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_RELEASE_ASSET

  //TODO username instead of address
  def releaseAssetForm(blockchainAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(blockchainAddress = blockchainAddress, pegHash = pegHash))
  }

  def releaseAsset(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.FormField.BLOCKCHAIN_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name)))
          }
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
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
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

      def addressesUnderZone(zoneID: String) = blockchainACLAccounts.Service.getAddressesUnderZone(zoneID)

      def allLockedAssets(addressesUnderZone: Seq[String]) = blockchainAssets.Service.getAllLocked(addressesUnderZone)

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
        Future {
          BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors))
        }
      },
      releaseAssetData => {

        val post = transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = releaseAssetData.from, gas = releaseAssetData.gas.toString), to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, mode = releaseAssetData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_RELEASED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
