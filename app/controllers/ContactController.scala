package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact

import scala.concurrent.ExecutionContext

@Singleton
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterContacts: master.Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  def updateContactForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact(countryCallingCodes = constants.CountryCallingCode.COUNTRY_CODES))
  }

  def updateContact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.updateContact(formWithErrors, constants.CountryCallingCode.COUNTRY_CODES))
        },
        updateContactData => {
          try {
            masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, updateContactData.emailAddress)
            masterAccounts.Service.updateStatusUnverifiedContact(loginState.username)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def contact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.contact(masterContacts.Service.getContact(loginState.username)))
      } catch {
        case _: BaseException => InternalServerError
      }
  }
}