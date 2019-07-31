package controllers

import controllers.actions.WithLoginAction
import javax.inject.{Inject, Singleton}
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.LoginState
import views.companion.master.UpdateContact

import scala.concurrent.ExecutionContext

@Singleton
class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def updateContactForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact(UpdateContact.form, constants.CountryCallingCode.COUNTRY_CODES))
  }

  def updateContact(): Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.updateContact(formWithErrors, constants.CountryCallingCode.COUNTRY_CODES))
        },
        updateContactData => {
          implicit val loginState:LoginState = LoginState(username)
          if (contacts.Service.insertOrUpdateEmailAndMobile(username, updateContactData.countryCode + updateContactData.mobileNumber, updateContactData.emailAddress)) Ok(views.html.index(successes = Seq(constants.Response.SUCCESS))) else Ok(views.html.index(failures = Seq(constants.Response.FAILURE)))
        }
      )
  }
}