package controllers

import javax.inject.Inject
import models.master
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms._

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: master.Accounts)(implicit exec: ExecutionContext) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def login = Action { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, formWithErrors, UpdateContact.form, VerifyEmailAddress.form, VerifyMobileNumber.form, SendEmailAddressVerification.form, SendMobileNumberVerification.form))
      },
      loginData => {
        if (accounts.Service.validateLogin(loginData.username, loginData.password)) Ok("Login") else Ok("Incorrect  Password")
      })
  }
}
