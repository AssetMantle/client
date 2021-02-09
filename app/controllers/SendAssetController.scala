package controllers

import controllers.actions.{WithTraderLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.blockchain.SendAsset

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                    transactionsSendAsset: SendAsset,
                                    withoutLoginAction: WithoutLoginAction,
                                    withoutLoginActionAsync: WithoutLoginActionAsync,
                                   )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SEND_ASSET


  def blockchainSendAssetForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.sendAsset())
  }

  def blockchainSendAsset: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
    views.companion.blockchain.SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sendAsset(formWithErrors)))
      },
      sendAssetData => {
        val post = transactionsSendAsset.Service.post(transactionsSendAsset.Request(transactionsSendAsset.BaseReq(from = sendAssetData.from, gas = sendAssetData.gas), to = sendAssetData.to, password = sendAssetData.password, pegHash = sendAssetData.pegHash, mode = sendAssetData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_SENT)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
