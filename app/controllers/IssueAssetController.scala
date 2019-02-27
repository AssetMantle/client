package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.IssueAsset
import views.companion.blockchain.IssueAsset

import scala.concurrent.ExecutionContext

class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionIssueAsset: IssueAsset)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def issueAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueAsset(IssueAsset.form))
  }

  def issueAsset: Action[AnyContent] = Action { implicit request =>
    IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueAsset(formWithErrors))
      },
      issueAssetData => {
        try {
          Ok(views.html.index(transactionIssueAsset.Service.post(new transactionIssueAsset.Request(issueAssetData.from, issueAssetData.to, issueAssetData.documentHash, issueAssetData.assetType, issueAssetData.assetPrice, issueAssetData.quantityUnit, issueAssetData.assetQuantity, issueAssetData.chainID, issueAssetData.password, issueAssetData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
