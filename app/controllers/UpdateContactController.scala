package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact
import models.master
import scala.concurrent.ExecutionContext

@Singleton
class UpdateContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, contacts: Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
        signUpData => {
          try {
            contacts.Service.insertOrUpdateContact(loginState.username, signUpData.countryCode + signUpData.mobileNumber, signUpData.emailAddress)
            masterAccounts.Service.updateStatusUnverifiedContact(loginState.username)
            Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          } catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}