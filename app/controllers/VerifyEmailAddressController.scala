package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Contact
import models.masterTransaction.EmailOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, emailOTPs: EmailOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_EMAIL

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val emailAddress: Future[String] = masterContacts.Service.getUnverifiedEmailAddress(loginState.username)
      val otp = emailOTPs.Service.sendOTP(loginState.username)

      def sendNotificationAndGetResult(emailAddress: String, otp: String): Future[Result] = {
        utilitiesNotification.sendEmailToEmailAddress(toEmailAddress = emailAddress, email = constants.Notification.VERIFY_EMAIL.email.get, otp)
        withUsernameToken.Ok(views.html.component.master.verifyEmailAddress())
      }

      (for {
        emailAddress <- emailAddress
        otp <- otp
        result <- sendNotificationAndGetResult(emailAddress = emailAddress, otp = otp)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyEmailAddress.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.verifyEmailAddress(formWithErrors)))
        },
        verifyEmailAddressData => {
          val verifyOTP = emailOTPs.Service.verifyOTP(loginState.username, verifyEmailAddressData.otp)

          def verifyEmailAddress = masterContacts.Service.verifyEmailAddress(loginState.username)

          def contact: Future[Contact] = masterContacts.Service.getOrNoneContact(loginState.username).map { contact => contact.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

          def updateStatus(contact: Contact): Future[Int] = {
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedMobile(loginState.username)
            }
          }

          (for {
            _ <- verifyOTP
            _ <- verifyEmailAddress
            contact <- contact
            _ <- updateStatus(contact)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.EMAIL_ADDRESS_VERIFIED)))
          } yield result
            ).recover {
            case baseException: BaseException => if (baseException.failure == constants.Response.INVALID_OTP) BadRequest(views.html.component.master.verifyEmailAddress(views.companion.master.VerifyEmailAddress.form.withError(constants.FormField.OTP.name, constants.Response.INVALID_OTP.message))) else InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
