package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.RedeemAsset
import views.companion.blockchain.RedeemAsset

import scala.concurrent.ExecutionContext

class RedeemAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionRedeemAsset: RedeemAsset)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def redeemAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.redeemAsset(RedeemAsset.form))
  }

  def redeemAsset: Action[AnyContent] = Action { implicit request =>
    RedeemAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.redeemAsset(formWithErrors))
      },
      redeemAssetData => {
        try {
          transactionRedeemAsset.Service.post(new transactionRedeemAsset.Request(redeemAssetData.from, redeemAssetData.password, redeemAssetData.to, redeemAssetData.pegHash, redeemAssetData.chainID, redeemAssetData.gas))
          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
