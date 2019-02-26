package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.ReleaseAsset
import views.companion.blockchain.ReleaseAsset

import scala.concurrent.ExecutionContext

class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionReleaseAsset: ReleaseAsset)(implicit exec: ExecutionContext, configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def releaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.releaseAsset(ReleaseAsset.form))
  }

  def releaseAsset: Action[AnyContent] = Action { implicit request =>
    ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          Ok(views.html.index(transactionReleaseAsset.Service.post(new transactionReleaseAsset.Request(releaseAssetData.from, releaseAssetData.to, releaseAssetData.pegHash, releaseAssetData.chainID, releaseAssetData.password, releaseAssetData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
