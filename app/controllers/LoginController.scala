package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import models.master
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms.Login

import scala.concurrent.ExecutionContext

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: master.Accounts) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def login = Action { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors))
      },
      loginData => {
        if (accounts.Service.validateLogin(loginData.username, loginData.password)) Ok("Login") else Ok("Incorrect  Password")
      })
  }
}
