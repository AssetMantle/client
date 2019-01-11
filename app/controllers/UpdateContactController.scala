package controllers

import javax.inject.Inject
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms._

import scala.concurrent.ExecutionContext

class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts)(implicit exec: ExecutionContext) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def updateContact = Action { implicit request =>
    UpdateContact.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, Login.form, formWithErrors, VerifyEmailAddress.form, VerifyMobileNumber.form))
      },
      signUpData => {
        if (contacts.Service.updateEmailAndMobile(signUpData.username, signUpData.mobileNumber, signUpData.emailAddress)) Ok("Updated") else Ok("Problem")
      })
  }
}
