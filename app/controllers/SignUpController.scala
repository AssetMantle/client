package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.SignUp

import scala.concurrent.ExecutionContext

@Singleton
class SignUpController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, blockchainAccounts: models.blockchain.Accounts)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.CONTROLLERS_SIGN_UP

  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.signUp(SignUp.form))
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (accounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.signUp(formWithErrors))
      },
      signUpData => {
        try {
          accounts.Service.addLogin(signUpData.username, signUpData.password, blockchainAccounts.Service.create(signUpData.username, signUpData.password), request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))
          Ok(views.html.index(successes = Seq(constants.Response.SIGNED_UP)))
        } catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}