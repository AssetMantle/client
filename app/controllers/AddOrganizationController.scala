package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.AddOrganization
import views.companion.{blockchain, master}

import scala.concurrent.ExecutionContext

class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddOrganization: AddOrganization)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def addOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addOrganization(master.AddOrganization.form))
  }

  def addOrganization: Action[AnyContent] = Action { implicit request =>
    master.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          Ok(views.html.index(success = transactionAddOrganization.Service.post(new transactionAddOrganization.Request(addOrganizationData.from, addOrganizationData.to, addOrganizationData.organizationID, addOrganizationData.chainID, addOrganizationData.password)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }


  def addOrganizationFormBC: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization(blockchain.AddOrganization.form))
  }

  def addOrganizationBC: Action[AnyContent] = Action { implicit request =>
    blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          Ok(views.html.index(success = transactionAddOrganization.Service.post(new transactionAddOrganization.Request(addOrganizationData.from, addOrganizationData.to, addOrganizationData.organizationID, addOrganizationData.chainID, addOrganizationData.password)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}