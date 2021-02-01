package controllers

import controllers.actions.{WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
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
                                       transactionsReleaseAsset: transactions.ReleaseAsset,
                                       withoutLoginAction: WithoutLoginAction,
                                       withoutLoginActionAsync: WithoutLoginActionAsync,
                                       withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_RELEASE_ASSET

  def blockchainReleaseAssetForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.releaseAsset())
  }

  def blockchainReleaseAsset: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors)))
      },
      releaseAssetData => {
        val postRequest = transactionsReleaseAsset.Service.post(transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = releaseAssetData.from, gas = releaseAssetData.gas), to = releaseAssetData.to, password = releaseAssetData.password, pegHash = releaseAssetData.pegHash, mode = releaseAssetData.mode))
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
