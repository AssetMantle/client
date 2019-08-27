package controllers

import controllers.actions.WithLoginAction
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
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterContacts: master.Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  def updateContactForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact(UpdateContact.form, constants.CountryCallingCode.COUNTRY_CODES))
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
            Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          } catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def getContact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(Json.toJson(masterContacts.Service.getContact(loginState.username)))
      } catch {
        case _: BaseException => NoContent
      }
  }
}