package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.master.Contact
import models.masterTransaction.SMSOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, smsOTPs: SMSOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_SMS

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val otp = smsOTPs.Service.sendOTP(loginState.username)

      def sendNotificationAndGetResult(otp: String) = {
        utilitiesNotification.send(accountID = loginState.username, notification = constants.Notification.VERIFY_PHONE, otp)
        withUsernameToken.Ok(views.html.component.master.verifyMobileNumber())
      }

      (for {
        otp <- otp
        result <- sendNotificationAndGetResult(otp)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors)))
        },
        verifyMobileNumberData => {
          val verifyOTP = smsOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)
          val verifyMobileNumber = masterContacts.Service.verifyMobileNumber(loginState.username)

          def contact: Future[Contact] = masterContacts.Service.getOrNoneContact(loginState.username).map { contactVal => contactVal.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)) }

          def updateStatus(contact: Contact): Future[Int] = {
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedEmail(loginState.username)
            }
          }

          (for {
            _ <- verifyOTP
            _ <- verifyMobileNumber
            contact <- contact
            _ <- updateStatus(contact)
            result <- withUsernameToken.Ok(views.html.profile(successes = Seq(constants.Response.SUCCESS)))
          } yield {
            utilitiesNotification.createNotificationAndSend(loginState.username, None, constants.Notification.PHONE_VERIFIED, loginState.username)
            result}
            ).recover {
            case baseException: BaseException => if (baseException.failure == constants.Response.INVALID_OTP) BadRequest(views.html.component.master.verifyMobileNumber(views.companion.master.VerifyMobileNumber.form.withError(constants.FormField.OTP.name, constants.Response.INVALID_OTP.message))) else InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}