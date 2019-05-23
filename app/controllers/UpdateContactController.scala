package controllers

import controllers.actions.WithLoginAction
import javax.inject.{Inject, Singleton}
import models.master.Contacts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact

import scala.concurrent.ExecutionContext

@Singleton
class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def updateContactForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact(UpdateContact.form, constants.CountryCallingCode.COUNTRY_CODES))
  }

  def updateContact: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.updateContact(formWithErrors, constants.CountryCallingCode.COUNTRY_CODES))
        },
        signUpData => {
          if (contacts.Service.updateEmailAndMobile(username, signUpData.countryCode + signUpData.mobileNumber, signUpData.emailAddress)) Ok(views.html.index(successes = Messages(constants.Flash.SUCCESS))) else Ok(views.html.index(failures =Messages(constants.Flash.FAILURE)))
        }
      )
  }
}