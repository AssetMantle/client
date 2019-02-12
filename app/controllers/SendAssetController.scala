package controllers

import exceptions.BaseException
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendAsset
import views.companion.blockchain.SendAsset

import scala.concurrent.ExecutionContext

class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSendAsset: SendAsset)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.sendAsset(SendAsset.form))
  }

  def sendAsset: Action[AnyContent] = Action { implicit request =>
    SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.sendAsset(formWithErrors))
      },
      sendAssetData => {
        try {
          transactionSendAsset.Service.post(new transactionSendAsset.Request(sendAssetData.from, sendAssetData.password, sendAssetData.to, sendAssetData.pegHash, sendAssetData.chainID, sendAssetData.gas))
          Ok("")
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
