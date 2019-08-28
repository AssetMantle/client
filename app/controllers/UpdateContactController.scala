package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact

import scala.concurrent.ExecutionContext

@Singleton
class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def updateContactForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact(UpdateContact.form, constants.CountryCallingCode.COUNTRY_CODES))
  }

  def updateContact(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.updateContact(formWithErrors, constants.CountryCallingCode.COUNTRY_CODES))
        },
        updateContactData => {
          try {
            contacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, updateContactData.emailAddress)
            masterAccounts.Service.updateStatusUnverifiedContact(loginState.username)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}