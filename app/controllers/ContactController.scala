package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Contact
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                  utilitiesNotification: utilities.Notification,
                                  masterContacts: master.Contacts,
                                  withLoginAction: WithLoginAction,
                                  masterAccounts: master.Accounts,
                                  withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONTACT

  implicit val contactWrites: OWrites[master.Contact] = Json.writes[master.Contact]

  def updateContactForm(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterContacts.Service.get(loginState.username)

      (for {
        contact <- contact
      } yield {
        contact match {
          case Some(contact) =>
            Ok(views.html.component.master.updateContact(views.companion.master.UpdateContact.form.fill(value = views.companion.master.UpdateContact.Data(emailAddress = contact.emailAddress, mobileNumber = contact.mobileNumber.takeRight(10), countryCode = contact.mobileNumber.dropRight(10)))))
          case None => Ok(views.html.component.master.updateContact())
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateContact(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      UpdateContact.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateContact(formWithErrors)))
        },
        updateContactData => {

          val emailPresent = masterContacts.Service.emailPresent(updateContactData.emailAddress, loginState.username)

          val mobilePresent = masterContacts.Service.mobileNumberPresent(updateContactData.countryCode + updateContactData.mobileNumber, loginState.username)

          def getResult(emailPresent: Boolean, mobilePresent: Boolean) = {
            if (mobilePresent && emailPresent) {
              Future(BadRequest(views.html.component.master.updateContact(views.companion.master.UpdateContact.form.fill(value = views.companion.master.UpdateContact.Data(emailAddress = updateContactData.emailAddress, mobileNumber = updateContactData.mobileNumber, countryCode = updateContactData.countryCode)).withError(constants.FormField.EMAIL_ADDRESS.name, constants.Response.EMAIL_ADDRESS_ALREADY_IN_USE.message).withError(constants.FormField.MOBILE_NUMBER.name, constants.Response.MOBILE_NUMBER_ALREADY_IN_USE.message))))
            }
            else if (mobilePresent) {
              Future(BadRequest(views.html.component.master.updateContact(views.companion.master.UpdateContact.form.fill(value = views.companion.master.UpdateContact.Data(emailAddress = updateContactData.emailAddress, mobileNumber = updateContactData.mobileNumber, countryCode = updateContactData.countryCode)).withError(constants.FormField.MOBILE_NUMBER.name, constants.Response.MOBILE_NUMBER_ALREADY_IN_USE.message))))
            }
            else if (emailPresent) {
              Future(BadRequest(views.html.component.master.updateContact(views.companion.master.UpdateContact.form.fill(value = views.companion.master.UpdateContact.Data(emailAddress = updateContactData.emailAddress, mobileNumber = updateContactData.mobileNumber, countryCode = updateContactData.countryCode)).withError(constants.FormField.EMAIL_ADDRESS.name, constants.Response.EMAIL_ADDRESS_ALREADY_IN_USE.message))))
            }
            else {

              val contact= masterContacts.Service.get(loginState.username)

              def updateContact(contactDetails: Option[Contact])={
                contactDetails match{
                  case Some(contact)=>{
                    if(contact.mobileNumber != (updateContactData.countryCode + updateContactData.mobileNumber) && contact.emailAddress != updateContactData.emailAddress) masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, false, updateContactData.emailAddress, false)
                    else if(contact.mobileNumber != (updateContactData.countryCode + updateContactData.mobileNumber)) masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, false, updateContactData.emailAddress, contact.emailAddressVerified)
                    else if(contact.emailAddress != updateContactData.emailAddress) masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, contact.mobileNumberVerified, updateContactData.emailAddress, false)
                    else Future{}
                  }
                  case None =>  masterContacts.Service.insertOrUpdateContact(loginState.username, updateContactData.countryCode + updateContactData.mobileNumber, false, updateContactData.emailAddress, false)
                }
              }

              def updateStatusUnverifiedContact: Future[Int] = masterAccounts.Service.updateStatusUnverifiedContact(loginState.username)

              for {
                contact<-contact
                _ <- updateContact(contact)
                _ <- updateStatusUnverifiedContact
                result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.CONTACT_UPDATED)))
              } yield result
            }
          }

          (for {
            emailPresent <- emailPresent
            mobilePresent <- mobilePresent
            result <- getResult(emailPresent, mobilePresent)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.CONTACT_UPDATED, loginState.username)
          } yield {
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def contact: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val contact = masterContacts.Service.get(loginState.username)
      (for {
        contact <- contact
      } yield Ok(views.html.component.master.contact(contact))
        ).recover {
        case _: BaseException => InternalServerError
      }
  }
}