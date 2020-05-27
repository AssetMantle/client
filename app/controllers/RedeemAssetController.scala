package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RedeemAssetController @Inject()(
                                       messagesControllerComponents: MessagesControllerComponents,
                                       transactionsRedeemAsset: transactions.RedeemAsset,
                                       withoutLoginAction: WithoutLoginAction,
                                       withoutLoginActionAsync: WithoutLoginActionAsync,
                                     )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_REDEEM_ASSET

  def blockchainRedeemAssetForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.redeemAsset())
  }

  def blockchainRedeemAsset: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.RedeemAsset
      .form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.redeemAsset(formWithErrors)))
      },
      redeemAssetData => {
        val post = transactionsRedeemAsset.Service.post(transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseReq(from = redeemAssetData.from, gas = redeemAssetData.gas.toString), to = redeemAssetData.to, password = redeemAssetData.password, pegHash = redeemAssetData.pegHash, mode = redeemAssetData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_REDEEMED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
