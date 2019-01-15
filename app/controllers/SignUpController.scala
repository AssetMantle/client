package controllers

import javax.inject.Inject
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms._

import scala.concurrent.ExecutionContext

class SignUpController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: master.Accounts, accounts_bc: blockchain.Accounts)(implicit exec: ExecutionContext) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def signUp = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors, Login.form, UpdateContact.form, VerifyEmailAddress.form, VerifyMobileNumber.form, SendEmailAddressVerification.form, SendMobileNumberVerification.form))
      },
      signUpData => {
        val x = accounts.Service.addLogin(signUpData.username, signUpData.password, accounts_bc.Service.addAccount())
        Ok(s"Sign up $x")
      })
  }

}