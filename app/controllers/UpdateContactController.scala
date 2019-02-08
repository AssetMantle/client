package controllers

import controllers.actions.WithLoginAction
import javax.inject.Inject
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.UpdateContact

import scala.concurrent.ExecutionContext

class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def updateContactForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.updateContact(UpdateContact.form))
  }

  def updateContact: Action[AnyContent] = withLoginAction { implicit request =>
    UpdateContact.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.updateContact(formWithErrors))
      },
      signUpData => {
        if (contacts.Service.updateEmailAndMobile(request.session.get(constants.Security.USERNAME).get, signUpData.mobileNumber, signUpData.emailAddress)) Ok(views.html.index(success = "Contact Updated!")) else Ok(views.html.index(failure = "Signup Failed!"))
      })
  }
}