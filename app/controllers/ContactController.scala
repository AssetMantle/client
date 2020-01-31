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

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterContacts: master.Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  def updateContactForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.updateContact())
  }

  def updateContact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          Future (BadRequest(views.html.component.master.updateContact(formWithErrors)))
        },
        updateContactData => {
          val insertOrUpdateContact = masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, updateContactData.emailAddress)

          def updateStatusUnverifiedContact: Future[Int] = masterAccounts.Service.updateStatusUnverifiedContact(loginState.username)

          (for {
            _ <- insertOrUpdateContact
            _ <- updateStatusUnverifiedContact
            result<-withUsernameToken.Ok(views.html.component.master.profile(successes = Seq(constants.Response.CONTACT_UPDATED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def contact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterContacts.Service.getContact(loginState.username)
      (for {
        contact <- contact
      } yield Ok(views.html.component.master.contact(contact))
        ).recover {
        case _: BaseException => InternalServerError
      }
  }
}